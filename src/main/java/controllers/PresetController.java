package controllers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import helpers.JwtMall;
import models.Preset;
import models.User;
import repositories.HibernatePresetRepository;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static spark.Spark.halt;

public class PresetController {
    private final HibernatePresetRepository hibernatePresetRepository;

    public PresetController(HibernatePresetRepository hibernatePresetRepository) {
        this.hibernatePresetRepository = hibernatePresetRepository;
    }

    public String create(Request request, Response response) {
        String name = request.queryParams("name");
        String description = request.queryParams("description");
        String directValue = request.queryParams("directValue");
        User user = JwtMall.getUserFromJwt(request);

        Preset preset = new Preset(directValue, user);
        preset.setName(name);
        preset.setDescription(description);
        hibernatePresetRepository.save(preset);

        try {
            response.status(HttpServletResponse.SC_CREATED);
            return preset.toJson();
        } catch (JsonProcessingException jsonParseException) {
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "The preset was saved. However, there was an issue serializing the preset in the response. You most likely can ignore this.";
        }
    }

    public String get(Request request, Response response) {
        String presetId = request.params(":presetId");
        Optional<Preset> preset = hibernatePresetRepository.findById(Long.parseLong(presetId));

        if(preset.isEmpty()) {
            halt(HttpServletResponse.SC_NOT_FOUND, "Preset not found.");
        }

        try {
            response.status(HttpServletResponse.SC_OK);
            return preset.get().toJson();
        } catch (JsonProcessingException jsonParseException) {
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "There was an issue serializing the preset in the response. You most likely can ignore this.";
        }
    }
}
