package com.worldonetop.portfolio.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.worldonetop.portfolio.BuildConfig
import com.worldonetop.portfolio.R
import com.worldonetop.portfolio.data.model.Activitys
import com.worldonetop.portfolio.data.model.LinkInfo
import com.worldonetop.portfolio.data.model.Portfolio
import com.worldonetop.portfolio.data.model.Question
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.net.UnknownHostException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject



class FileUtil @Inject constructor(@ApplicationContext private val context: Context) {
    companion object{
        enum class Type{
            Cache, Resume, Activity
            // 임시, 이력서, 활동들
        }
    }

    /** normal file util */

    // 공용 저장소에서 가져온 파일을 따로 저장하기 위해서(viewer 때문에 공용 저장소에 보관)
    // originUri - getContent intent 로 가져온 uri
    // type - 상위 디렉토리 구분
    // id - 필요할 경우 db id 별로 구분해서 저장
    // return - success("확장자를 포함한 파일명") or fail(null)
    suspend fun downloadFile(originUri: Uri, type:Type, id: Int? = null):String?{
        // 파일 명 및 위치 설정
        val fileName = uriToFileName(originUri) ?: "unidentified file"
        val newFile = getNewFile(fileName, type, id)

        // 복붙
        context.contentResolver.openInputStream(originUri)?.use { inputStream ->
            try {
                withContext(Dispatchers.IO) {
                    inputStream.copyTo(newFile.outputStream())
                }
            } catch (e: Exception){
                inputStream.close()
                e.printStackTrace()
                return null
            }
        }
        return newFile.name
    }
    // 해당 파일 부분 삭제
    suspend fun removeFile(fileName: List<String>, type: Type, id: Int?=null){
        val baseFile = getBaseFile(type, id)
        withContext(Dispatchers.IO) {
            for(name in fileName){
                File(baseFile, name).apply {
                    if(exists())
                        delete()
                }
            }
        }
    }
    // 저장할 캐시 폴더를 저장할 위치로
    suspend fun moveCacheTo(typeTo: Type,fromName:List<String>, idTo: Int?=null){
        withContext(Dispatchers.IO) {
            for(fn in fromName){
                val newFile = getNewFile(fn, typeTo, idTo)
                val oldFile = File(context.externalCacheDir, fn)
                oldFile.copyTo(newFile)
                oldFile.delete()
            }
        }
    }


    /** og tag */
    suspend fun getLinkInfo(url:String):LinkInfo? = coroutineScope {
        val fileName = url.replace(".","").replace("/","")
        val file = File(context.externalCacheDir,fileName)
        var result: LinkInfo? = null

        if(!file.exists()){
            withContext(Dispatchers.IO) {
                val urlParse = if (!url.startsWith("http://") && !url.startsWith("https://"))
                    "http://$url"
                else url

                val document: Document?
                try {
                    document = Jsoup.connect(urlParse).get()
                }catch (e:UnknownHostException){
                    return@withContext
                }

                result = LinkInfo(file.path)
                document?.select("meta[property^=og:]")?.let {
                    try{
                        it.forEach { el ->
                            when(el.attr("property")) {
                                "og:title" -> {
                                    result!!.title = el.attr("content")
                                }
                                "og:description" -> {
                                    result!!.description = el.attr("content")
                                }
                                "og:image" -> {
                                    result!!.image = el.attr("content")
                                }
                            }
                        }

                        file.writeText(Gson().toJson(result))
                    }catch (e:Exception){
                        result = null
                        e.printStackTrace()
                    }
                }
            }
        }else{
            try {
                result = Gson().fromJson(file.readText(), LinkInfo::class.java)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        result
    }
    suspend fun removeLinkInfo(linkInfo: LinkInfo?){
        linkInfo?.let {
            withContext(Dispatchers.IO){
                File(it.filePath).apply {
                    if(exists()){
                        launch { Glide.with(context).asFile().load(it.image).submit().get().delete() }
                        launch { delete() }
                    }

                }
            }
        }
    }

    /** shared data */
    suspend fun removeShasredData(fileName: String) = withContext(Dispatchers.IO){
        launch { File(context.externalCacheDir, fileName).delete() }
        launch { File(context.externalCacheDir, "$fileName.zip").delete() }
    }
    suspend fun createSharedActivitys(data: List<Activitys>, fileName: String):File = withContext(Dispatchers.IO){
        val rootFile = File(context.externalCacheDir, fileName)
        rootFile.deleteOnExit()
        rootFile.mkdir()
        for(i in data.indices){
            var subFile = File(rootFile, data[i].title)
            if(subFile.exists()){
                var suffix = 1
                do {
                    subFile = File(rootFile, data[i].title+suffix)
                    suffix += 1
                }while (subFile.exists())
            }
            subFile.mkdir()

            File(subFile, "info.txt").writeText(createSharedTextData(data[i]))
            for(subFileName in data[i].files){
                val originFile = File(getBaseFile(Type.Activity,data[i].activityId), subFileName)
                originFile.copyTo(File(subFile,originFile.name))
            }
        }
        rootFile
    }
    suspend fun createSharedPortfolio(data: List<Portfolio>,
                                      fileName: String, activityData:Map<Int,List<Activitys>>,
                                      questionData:Map<Int,List<Question>>):File = withContext(Dispatchers.IO){
        val rootFile = File(context.externalCacheDir, fileName)
        rootFile.deleteOnExit()
        rootFile.mkdir()
        for(i in data.indices){
            val subFileName = data[i].title.substring(0,data[i].title.lastIndexOf("."))
            var subFile = File(rootFile, subFileName)
            if(subFile.exists()){
                var suffix = 1
                do {
                    subFile = File(rootFile, subFileName+suffix)
                    suffix += 1
                }while (subFile.exists())
            }
            subFile.mkdir()

            if(!data[i].content.isNullOrBlank())
                File(subFile, "info.txt").writeText(createSharedTextData(data[i]))

            activityData.getOrDefault(data[i].portfolioId,null)?.let {
                createSharedActivitys(it, "$fileName/${subFile.name}/${context.getString(R.string.tab_activity)}")
            }
            questionData.getOrDefault(data[i].portfolioId,null)?.let {
                createSharedQuestion(it, "$fileName/${subFile.name}/${context.getString(R.string.tab_qna)}.txt")
            }
            val originFile = File(getBaseFile(Type.Resume,null), data[i].title)
            originFile.copyTo(File(subFile,originFile.name))
        }
        rootFile
    }
    // 관련 문답은 한 파일에 몰아 쓴 형식
    suspend fun createSharedQuestion(data: List<Question>, fileName: String):File = withContext(Dispatchers.IO){
        val file = File(context.externalCacheDir, fileName)
        file.deleteOnExit()
        val stream = file.outputStream()
        for(i in 0 until data.size-1){
            stream.write(createSharedTextData(data[i]).toByteArray())
            stream.write("\n".toByteArray())
            stream.write("-----".repeat(6).toByteArray()) // divider
            stream.write("\n".toByteArray())
        }
        stream.write(createSharedTextData(data.last()).toByteArray())
        stream.close()
        file
    }
    suspend fun makeZipFolder(targetFile: File):File = withContext(Dispatchers.IO){
        val zipFile = File(context.externalCacheDir, targetFile.name + ".zip")
        val zipOutputStream = ZipOutputStream(zipFile.outputStream())
        makeZipEntry(zipOutputStream, targetFile, "")
        zipOutputStream.close()
        targetFile.delete()
        zipFile
    }
    private suspend fun makeZipEntry(zipOutputStream: ZipOutputStream,targetFile: File, parentPath: String):Unit = coroutineScope{
        val path =  if(parentPath.isBlank()) "" else "$parentPath/"

        if(targetFile.isDirectory){
            targetFile.listFiles()?.let {
                for(child in it){
                    makeZipEntry(zipOutputStream, child, path + targetFile.name)
                }
            }
        }else{
            zipOutputStream.putNextEntry(ZipEntry(path + targetFile.name))
            targetFile.inputStream().copyTo(zipOutputStream)
            zipOutputStream.closeEntry()
        }

        targetFile.delete()
    }

    /** view intent */
    // 해당 파일 명과 구분값을 받아서 actionView intent 리턴, null - no search file
    fun openFileIntent(fileName: String, type: Type, id: Int? = null):Intent {
        val uri= FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", File(getBaseFile(type,id), fileName))

        val intent = Intent(Intent.ACTION_VIEW)
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (fileName.endsWith(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (fileName.endsWith(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (fileName.endsWith(".wav") || fileName.endsWith(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (fileName.endsWith(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
        } else if (fileName.endsWith(".zip")) {
            // ZIP file
            intent.setDataAndType(uri, "application/zip")
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg")
        } else if (fileName.endsWith(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else if (fileName.endsWith(".3gp") || fileName
                .endsWith(".mpg") || fileName.endsWith(".mpeg") || fileName
                .endsWith(".mpe") || fileName.endsWith(".mp4") || fileName
                .endsWith(".avi")
        ) {
            // Video files
            intent.setDataAndType(uri, "video/*")
        } else {
            intent.setDataAndType(uri, "*/*")
        }
        return intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** private function */
    /** file util */
    private fun uriToFileName(uri:Uri):String?{
        context.contentResolver.query(uri, null, null, null, null)?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            if(nameIndex != -1)
                return it.getString(nameIndex)
        }
        return null
    }

    // type과 id에 따라 저장될 기본 디렉토리
    private fun getBaseFile(type: Type, id: Int? = null):File?{
        return if(type == Type.Cache){
            context.externalCacheDir
        }else{
            val directory = type.name + if(id != null) "/$id/" else "/"
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), directory)
        }
    }
    // 새로운 파일이 저장될 파일, 중복 이름의 경우 이름뒤에 숫자를 붙임
    private fun getNewFile(fileName: String, type: Type, id: Int? = null):File{
        val baseFile = getBaseFile(type,id)
        var newFile = File(baseFile, fileName)

        if(newFile.exists()){
            var count = 1
            var fileNameOnly: String
            var fileType=""
            fileName.lastIndexOf(".").let {
                if(it==-1)
                    fileNameOnly= fileName
                else {
                    fileNameOnly = fileName.substring(0,it)
                    fileType = fileName.substring(it)
                }
            }

            while(newFile.exists()){
                newFile = File(baseFile, fileNameOnly+count+fileType)
                count += 1
            }
        }
        newFile.parentFile?.mkdirs()
        return newFile
    }
    /** shared data */
    private fun createSharedTextData(data:Any):String{
        var text = when(data){
            is Activitys ->{
                "${context.getString(R.string.add_activity_title_short)} : ${data.title}\n" +
                        "${context.getString(R.string.category)} : ${context.resources.getStringArray(R.array.activityCategoryString)[data.type]}\n" +
                        "${context.getString(R.string.date_short)} : ${data.startDate} ~ " +
                        if(data.endDate != null) "${data.endDate}\n" else "\n" +
                        if(data.content != null) "${context.getString(R.string.add_content_short)} : ${data.content}" else ""
            }
            is Portfolio ->{
                if(data.content != null) "${context.getString(R.string.add_content_short)} : ${data.content}" else ""
            }
            is Question ->{
                "Q. ${data.question}\n\nA. ${data.answer}"
            }
            else -> return ""
        }

        (data as? Activitys)?.let {
            if(it.links.isNotEmpty())
                text += context.getString(R.string.add_links)
            for(link in it.links)
                text += "\n${link}"
        }

        return text
    }
}