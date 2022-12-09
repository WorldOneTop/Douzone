package com.worldonetop.portfolio.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.pathString


class FileUtil(private val BASE_PATH: String, private val context: Context) {

    // 공용 저장소에서 가져온 파일을 따로 저장하기 위해서(viewer 때문에 공용 저장소에 보관)
    // originUri - getContent intent 로 가져온 uri
    // activityId - Activity 탭에 속할 ID or 이력서파일(null) => 디렉토리 구분해서 다른 활동안의 파일 이름과 중복 할수있게
    // return - sucess("확장자를 포함한 파일명") or fail(null)
    fun downloadFile(originUri: Uri, activityId:Int?):String?{
        // 디렉토리 구분 및 생성
        val pathStr = converterBaseUrl(activityId)
        Environment.getExternalStoragePublicDirectory(pathStr).mkdirs()

        // 파일 명 및 위치 확정
        var fileName = "unidentified file"
        context.contentResolver.query(originUri, null, null, null, null)?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            if(nameIndex != -1)
                fileName = it.getString(nameIndex)
        }
        val absolutePath = getFilePathNoOverride(pathStr,fileName)

        // 복붙
        context.contentResolver.openInputStream(originUri)?.use { inputStream ->
            try {
                Files.copy(inputStream, absolutePath, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception){
                inputStream.close()
                e.printStackTrace()
                return null
            }

            inputStream.close()
        }
        return absolutePath.pathString
    }


    // 해당 파일 명과 id(activity 만)를 받아서 actionView intent 리턴, null - no search file
    fun openFileIntent(fileName: String, activityId: Int?):Intent? {
        val uri= pathToUri(fileName, activityId) ?: return null

        val intent = Intent(Intent.ACTION_VIEW)
        if (fileName.contains(".doc") || fileName.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (fileName.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (fileName.contains(".ppt") || fileName.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (fileName.contains(".xls") || fileName.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (fileName.contains(".zip") || fileName.contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav")
        } else if (fileName.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (fileName.contains(".wav") || fileName.contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav")
        } else if (fileName.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif")
        } else if (fileName.contains(".jpg") || fileName.contains(".jpeg") || fileName.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg")
        } else if (fileName.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else if (fileName.contains(".3gp") || fileName
                .contains(".mpg") || fileName.contains(".mpeg") || fileName
                .contains(".mpe") || fileName.contains(".mp4") || fileName
                .contains(".avi")
        ) {
            // Video files
            intent.setDataAndType(uri, "video/*")
        } else {
            intent.setDataAndType(uri, "*/*")
        }
        return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }


    // 유형에 따른 디렉토리 구분
    // activityId - Activity 탭에 속할 ID or 이력서파일(null) => 디렉토리 구분해서 다른 활동안의 파일 이름과 중복 할수있게
    private fun converterBaseUrl(activityId:Int?) =
        if(activityId == null){
            BASE_PATH + "Portfolios/"
        }else{
            BASE_PATH + "Activitys/$activityId/"
        }

    //파일명 -> Uri
    private fun pathToUri(fileName: String, activityId: Int?): Uri? {
        val filePath = Environment.getExternalStoragePublicDirectory(converterBaseUrl(activityId) + fileName).path
        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"), null,
            "_data = '$filePath'", null, null
        )?.use {
            val idIndex = it.getColumnIndex("_id")
            it.moveToNext()
            try{
                return ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), it.getInt(idIndex).toLong())
            }catch (e: Exception){
                return null
            }
        }
        return null
    }

    // 해당 파일이 중복이더라도 뒤에 2, 3, 4.. 를 붙여 저장 가능한 Path 리턴
    private fun getFilePathNoOverride(base_path:String, fileName:String): Path {
        if(Files.exists(
                Paths.get(
                    Environment.getExternalStoragePublicDirectory(base_path).toString(),fileName
                )
            )
        ){
            var count = 2
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
            while(Files.exists(Paths.get(Environment.getExternalStoragePublicDirectory(base_path).toString(),
                    fileNameOnly+count+fileType))){
                count ++
            }

            return Paths.get(Environment.getExternalStoragePublicDirectory(base_path).toString(),
                fileNameOnly+count+fileType)
        }else{
            return Paths.get(Environment.getExternalStoragePublicDirectory(base_path).toString(),
                fileName)
        }
    }
}