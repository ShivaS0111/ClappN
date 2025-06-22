package biz.craftline.server.feature.businessstore.api.controller;

import biz.craftline.server.feature.businessstore.api.dto.StoreDTO;
import biz.craftline.server.feature.businessstore.api.mapper.StoreDTOMapper;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.util.APIResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/stores")
@RestController
public class StoreController {

    @Autowired
    StoreDTOMapper mapper;

    @Autowired
    private StoreService service;

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<Store>>> list() {
        List<Store> list = service.findAll();
        return APIResponse.success(list);
    }

    @PostMapping("/search")
    public ResponseEntity<APIResponse<List<StoreDTO>>> search(@RequestBody String keyword) {
        List<Store> list = service.findAll();
        List<StoreDTO> dtoList = list.stream().map( mapper::toDTO).toList();
        return APIResponse.success(dtoList);
    }

    //@PostMapping(name = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/add")
    public ResponseEntity<APIResponse<StoreDTO>> addStore(@RequestBody StoreDTO storeDTO) {
        Store store = service.save(mapper.toDomain(storeDTO));
        return APIResponse.success(mapper.toDTO(store));
    }


   /* @PostMapping(name = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse<StoreDTO>> add(@RequestBody StoreDTO storeDTO) {
        //System.out.println("===>StoreDTO: "+storeDTO.toString());
        Store entity = service.save(StoreDTO.getStore(storeDTO));
        return ResponseEntity.ok(APIResponse.success(StoreDTO.getStoreDTO(entity)));
    }*/
}