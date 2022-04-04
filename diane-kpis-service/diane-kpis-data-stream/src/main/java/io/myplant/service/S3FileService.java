package io.myplant.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.myplant.integration.AWSS3ClientBaseV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class S3FileService extends AWSS3ClientBaseV2 {
    private static final Logger logger = LoggerFactory.getLogger(S3FileService.class);
    private static final String s3SessionName = "diane-kpis";

    @Value("${aws.s3.key}")
    private String s3KeyFiles;

    @Value("${aws.s3.bucket-name}")
    private String bucket;


//    public InputStream downloadFile(String bucket, String key) {
//        AmazonS3Client s3Client = getS3Client(s3SessionName);
//        S3Object object = s3Client.getObject(new GetObjectRequest(bucket, key));
//        return object.getObjectContent();
//    }

    public void uploadFile(String key, File source) {
        AmazonS3Client s3Client = getS3Client(s3SessionName);
        s3Client.putObject(new PutObjectRequest(bucket, s3KeyFiles + key, source));
    }
//
//    public List<UploadedFile> listFiles(String bucket, String keyPrefix) throws UnsupportedEncodingException {
//        AmazonS3Client s3Client = getS3Client(s3SessionName);
//        ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucket).withPrefix(keyPrefix);
//        ObjectListing objectListing;
//        List<UploadedFile> manuals = new LinkedList<>();
//        do {
//            objectListing = s3Client.listObjects(listObjectsRequest);
//            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
//
//                String key = objectSummary.getKey();
//                String fileName = getKeySuffix(key);
//                String name = getKeySuffix(key);
//                long mody = 0;
//                if(fileName.contains("__")){
//                    //check if file name contains modyTimestamp
//                    try {
//                        String[] split = fileName.split("__");
//                        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                        String dateString = split[0].replace("_", ":");
//                        Date result1 = df1.parse(dateString);
//                        name = split[1];
//                        mody = result1.getTime();
//                    }catch (Exception ex){
//                        logger.error("invalid modydate of file: " + fileName, ex);
//                    }
//                }
//
//                String modelSerailKey = key.replace(s3KeyFiles +"/", "").replace("/"+fileName,"");
//                String[] split = modelSerailKey.split("/");
//                String model = "";
//                String serial = "";
//                if(split.length == 2){
//                    model = split[0];
//                    serial = split[1];
//                }
//
//                long size = objectSummary.getSize();
//                Date uploadTime = objectSummary.getLastModified();
//
//                //String href = ops.getSeshatBaseUrl() + DOWNLOAD_URL_FRAGMENT + key;
//                manuals.add(new UploadedFile(model, serial, name, fileName, size, uploadTime.getTime(), mody));
//            }
//            listObjectsRequest.setMarker(objectListing.getNextMarker());
//        }
//        while (objectListing.isTruncated());
//        return manuals;
//
//    }
//    private String getKeySuffix(String key) {
//        return StringUtils.isEmpty(key) ? "" : StringUtils.substringAfterLast(key, "/");
//    }
//
//    public S3Object download(String bucket, String awsKey) {
//        AmazonS3Client s3Client = getS3Client(s3SessionName);
//        S3Object s3object = s3Client.getObject(new GetObjectRequest(bucket, awsKey));
//        return s3object;
//    }
}
