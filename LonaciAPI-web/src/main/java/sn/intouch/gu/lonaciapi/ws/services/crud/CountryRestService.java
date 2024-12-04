package sn.intouch.gu.lonaciapi.ws.services.crud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.intouch.gu.lonaciapi.ejb.dto.CountryDTO;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.Country;
import sn.intouch.gu.lonaciapi.ejb.notification.services.CountryService;
import sn.intouch.gu.lonaciapi.ws.models.APIResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class CountryRestService {

    public final CountryService countryService = (CountryService) JNDIUtils
            .lookUpEJB(EJBRegistry.CountryServiceBean);

    @RequestMapping(value = "/api/v2/country/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getCountryById(@PathVariable String id){
        Country country = countryService.getByCode(id);
        if(country != null)
            return ResponseEntity.ok(APIResponse.<CountryDTO>builder()
                    .code(200)
                    .reason("SUCCESS")
                    .data(country.toDTO())
                    .build());
        return new ResponseEntity<>(APIResponse.<CountryDTO>builder()
                .code(404)
                .reason("Country not found")
                .build(), HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/api/v2/country", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<APIResponse> getAllCountry(){
        Iterable<Country> countries = countryService.findAll();

        return ResponseEntity.ok(APIResponse.<List<CountryDTO>>builder()
                .code(200)
                .reason("SUCCESS")
                .data(countryDTOList(countries))
                .build());
    }

    private List<CountryDTO> countryDTOList(Iterable<Country> countries){
        if(countries == null)
            return null;
        List<CountryDTO> list = new ArrayList<>();
        countries.forEach(cn -> list.add(cn.toDTO()));
        return list;
    }

    @RequestMapping(value = "/api/v2/country", method = RequestMethod.POST, produces = "application/json" , consumes = "application/json")
    public ResponseEntity<APIResponse> createCountry(@RequestBody CountryDTO countryDTO){
        Country country = countryService.getByCode(countryDTO.getCode());
        if(country != null)
            return new ResponseEntity<>(APIResponse.<CountryDTO>builder()
                    .code(201)
                    .reason("CREATE")
                    .build(),HttpStatus.CREATED);
        country = countryDTO.fromDTO();
        country = countryService.save(country);

        return ResponseEntity.ok(APIResponse.<CountryDTO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(country.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v2/country/{id}", method = RequestMethod.PATCH, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> updateCountry(@RequestBody CountryDTO countryDTO, @PathVariable String id){
        Country country = countryService.getByCode(id);
        if(country == null)
            return new ResponseEntity<>(APIResponse.<CountryDTO>builder()
                    .code(404)
                    .reason("Country not found")
                    .build(), HttpStatus.NOT_FOUND);

        transposeUpdate(country, countryDTO);
        country = countryService.save(country);
        return ResponseEntity.ok(APIResponse.<CountryDTO>builder()
                .code(200)
                .reason("SUCCESS")
                .data(country.toDTO())
                .build());
    }

    @RequestMapping(value = "/api/v2/country/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public ResponseEntity<APIResponse> deleteCountry(@PathVariable String id) {
        Country country = countryService.getByCode(id);
        if(country == null)
            return new ResponseEntity<>(APIResponse.<CountryDTO>builder()
                    .code(404)
                    .reason("Country not found")
                    .build(), HttpStatus.NOT_FOUND);
        countryService.delete(country);
        return ResponseEntity.ok(APIResponse.<CountryDTO>builder()
                .code(200)
                .reason("SUCCESS")
                .build());
    }

    private void transposeUpdate(Country country, CountryDTO dto) {
        if (dto.getCode() != null) country.setCode(dto.getCode());
        if (dto.getIndicatif() != null) country.setIndicatif(dto.getIndicatif());
        if (dto.getDevise() != null) country.setDevise(dto.getDevise());
        if (dto.getLangue() != null) country.setLangue(dto.getLangue());
        if (dto.getTimeZone() != null) country.setTimeZone(dto.getTimeZone());
        if (dto.getLibelle() != null) country.setLibelle(dto.getLibelle());
    }
}
