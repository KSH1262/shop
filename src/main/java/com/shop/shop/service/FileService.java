package com.shop.shop.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@Log
public class FileService {

    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception{
        UUID uuid = UUID.randomUUID(); // UUID : 서로 다른 개체들을 구별하기 위해서 이름을 부여할 때 사용
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        // UUID로 받은 값과 원래 파일의 이름의 화장자 조합해 저장될 파일 이름 생성
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        // 디렉토리가 없으면 생성
        File directory = new File(uploadPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); // 디렉토리와 상위 디렉토리 모두 생성
            if (created) {
                log.info("디렉토리 생성 성공: " + uploadPath);
            } else {
                log.warning("디렉토리 생성 실패 (권한 문제 등): " + uploadPath);
                throw new IOException("디렉토리 생성 실패: " + uploadPath);
            }
        }

        try {
            // FileOutputStream : 바이트 단위의 출력을 내보내는 클래스
            FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
            fos.write(fileData); // fileData 를 파일 출력 스트림에 입력
            fos.close();
            log.info("파일 업로드 성공: " + fileUploadFullUrl);
            return savedFileName;
        } catch (IOException e) { // IOException 을 명확히 catch
            log.severe("파일 업로드 중 오류 발생: " + e.getMessage()); // 오류 로그 (severe 레벨)
            e.printStackTrace(); // 스택 트레이스 출력
            throw new IOException("파일 업로드 실패: " + e.getMessage(), e); // 예외를 다시 던져 상위 계층에서 처리되도록 함
        }
    }

    public void deleteFile(String filePath) throws Exception{
        File deleteFile = new File(filePath); // 파일이 저장된 경로를 이용, 파일 객체 생성

        if (deleteFile.exists()) { // 해당 파일이 존재하면 파일 삭제
            if (deleteFile.delete()) { // 삭제 성공 여부 확인
                log.info("파일을 삭제하였습니다: " + filePath);
            } else {
                log.warning("파일 삭제 실패 (권한 문제 등): " + filePath); // 경고 로그
            }
        } else {
            log.info("삭제하려는 파일이 존재하지 않습니다: " + filePath);
        }
    }
}
