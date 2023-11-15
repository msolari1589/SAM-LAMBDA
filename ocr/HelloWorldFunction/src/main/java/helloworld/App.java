package helloworld;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.protobuf.ByteString;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");
        
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            String base64Image = input.getBody();
            if (base64Image == null || base64Image.isEmpty()) {
                return response
                        .withStatusCode(400) // Bad Request
                        .withBody("{\"error\": \"No se recibio la imagen en el cuerpo de la solicitud\"}");
            }
            ByteString imgBytes = ByteString.copyFrom(java.util.Base64.getDecoder().decode(base64Image));
            String output = Extractor.detectText(imgBytes);
            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Exception e) {
            return response
                    .withBody("{"+e.getMessage()+"}")
                    .withStatusCode(500);
        }
    }
}
