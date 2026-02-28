package es.codeurjc.grupo12.scissors_please.controller.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;

// IMPORTANTE: Usa esta importación, no la de Tomcat
import org.springframework.http.MediaType; 
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.service.BotService;

@Controller
public class ImageController {

    @Autowired
    private BotService botService;

    @GetMapping("/bot-images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<Bot> opBot = botService.getBotById(id);

        if (opBot.isPresent() && opBot.get().getImage() != null) {
            byte[] imageBytes = opBot.get().getImage().getData();
            
            String mimeType = null;
            try {
                mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
            } catch (IOException e) {
                // Si falla la detección, mimeType seguirá siendo null
            }
            
            // Usamos el MediaType de Spring
            MediaType contentType = (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(imageBytes);
        }
        
        return ResponseEntity.notFound().build();
    }
}