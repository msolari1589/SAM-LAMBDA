package helloworld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageContext;
import com.google.protobuf.ByteString;

public class Extractor {

    public static String detectText(ByteString imgBytes) throws IOException {
        if (imgBytes == null || imgBytes.isEmpty()) {
            return "{\"error\": \"No se recibio imagen en la llamada del metodo.\"}";
        }

        List<AnnotateImageRequest> requests = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        String textResult = "";

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
        ImageContext ic = ImageContext.newBuilder().addAllLanguageHints(getLanguageHints()).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img)
                .setImageContext(ic).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return null;
                }
                List<EntityAnnotation> textAnnotations = res.getTextAnnotationsList();

                if (!textAnnotations.isEmpty()) {
                    try {
                        String[] lines = textAnnotations.get(0).getDescription().replace("o", "0").replace("O", "0").split("\n");                        
                        JsonNode jsonNode = objectMapper.createObjectNode()
                                .put("lla", lines.length >= 1 ? extractNumber(lines[0].trim()) : -1)
                                .put("up", lines.length >= 2 ? extractNumber(lines[1].trim()) : -1)
                                .put("total", lines.length >= 3 ? extractNumber(lines[2].trim()) : -1)
                                .put("blancos", lines.length >= 4 ? extractNumber(lines[3].trim()) : -1)
                                .put("nulos", lines.length >= 5 ? extractNumber(lines[4].trim()) : -1)
                                .put("impugnados", lines.length >= 6 ? extractNumber(lines[5].trim()) : -1)
                                .put("recurridos", lines.length >= 7 ? extractNumber(lines[6].trim()) : -1);


                        textResult = objectMapper.writeValueAsString(jsonNode);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textResult;
    }

    private static List<String> getLanguageHints() {
        List<String> lenguajes = new ArrayList<>();
        lenguajes.add("en-419");
        lenguajes.add("en-t-i0-handwrit");
        return lenguajes;
    }

    private static int extractNumber(String input) {
        try {
            String digitsOnly = input.replaceAll("\\D", "");
            return Integer.parseInt(digitsOnly);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
