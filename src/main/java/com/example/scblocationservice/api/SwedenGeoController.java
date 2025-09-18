package com.example.scblocationservice.api;

import com.example.scblocationservice.dto.KommunDto;
import com.example.scblocationservice.dto.LanDto;
import com.example.scblocationservice.service.SwedenGeoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lan")
public class SwedenGeoController {


    private final SwedenGeoService catalog;

    public SwedenGeoController(SwedenGeoService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public List<LanDto> getAllLan() {
        return catalog.getAllLan();
    }

    @GetMapping("/{lanCode}/kommuner")
    public List<KommunDto> getKommuner(@PathVariable String lanCode) {
        return catalog.getKommunerByLan(lanCode);
    }

}

