package com.pbl6.services;

import com.pbl6.dtos.request.attribute.AddAttributeRequest;
import com.pbl6.dtos.request.attribute.EditAttributeRequest;
import com.pbl6.dtos.response.AttributeDto;

import java.util.List;

public interface AttributeService {
    List<AttributeDto> getFiltersByCateSlug(String slug);

    List<AttributeDto> getAllAttributeFilter(Long cateId);

    List<AttributeDto> getAllAttributeOption();

    void addAttributeValue(AddAttributeRequest attributeRequest);

    void editAttributeValue(Long valueId, EditAttributeRequest attributeRequest);

    void deleteAttributeValue(Long valueId);

}
