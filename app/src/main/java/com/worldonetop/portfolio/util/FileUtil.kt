package com.worldonetop.portfolio.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.worldonetop.portfolio.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject



class FileUtil @Inject constructor(@ApplicationContext private val context: Context) {
    companion object{
        enum class Type{
            Cache, Resume, Activity
            // 임시, 이력서, 활동들
        }
    }
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
                CoroutineScope(Dispatchers.IO).launch {
                    inputStream.copyTo(newFile.outputStream())
                }.join()
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
        for(name in fileName){
            File(baseFile, name).apply {
                if(exists())
                    delete()
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
            var fileNameOnly=""
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
        }
        else if (fileName.endsWith(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (fileName.endsWith(".wav") || fileName.endsWith(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (fileName.endsWith(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
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
}