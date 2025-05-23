package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Location;
import lombok.Data;

@Data
public class LocationDto {
    private Long id;
    private String name;
    private double latitude;
    private double longitude;

    public static LocationDto from(Location loc) {
        LocationDto dto = new LocationDto();
        dto.setId(loc.getId());
        dto.setName(loc.getName());
        dto.setLatitude(loc.getLatitude());
        dto.setLongitude(loc.getLongitude());
        return dto;
    }
}
