package es.codeurjc.mca.practica_1_cloud_ordinaria_2021.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.UUID;

@Service("storageService")
@Profile("production")
public class S3ImageService implements ImageService {

    @Value("${amazon.s3.region}")
    private String REGION;

    @Value("${amazon.s3.bucket-name}")
    private String BUCKET_NAME;

    @Value("${amazon.s3.endpoint}")
    private String ENDPOINT;

    public static AmazonS3 s3;

    @PostConstruct
    public void initializeS3( ) {
        s3 = AmazonS3ClientBuilder
            .standard()
            .withRegion(this.REGION)
            .build();

        if(!s3.doesBucketExistV2(this.BUCKET_NAME)) {
            s3.createBucket(this.BUCKET_NAME);
        }
    }

    @Override
    public String createImage(MultipartFile multiPartFile) {

        String fileName = "image_" + UUID.randomUUID() + "_" + multiPartFile.getOriginalFilename();
        File file = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
        try {
            multiPartFile.transferTo(file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, fileName, file);
            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
            s3.putObject(putObjectRequest);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't save image on s3", ex);
        }
        return s3.getUrl(BUCKET_NAME, fileName).toString();
    }

    @Override
    public void deleteImage(String image) {
        try {
            s3.deleteObject(BUCKET_NAME, image.replace(ENDPOINT, ""));
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't delete s3 image");
        }
    }
    
}
