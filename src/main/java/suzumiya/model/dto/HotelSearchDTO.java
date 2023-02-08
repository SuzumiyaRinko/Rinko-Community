package suzumiya.model.dto;

import lombok.Data;

@Data
public class HotelSearchDTO {

    private String key;
    private String brand;
    private String city;
    private String starName;
    private Integer minPrice;
    private Integer maxPrice;
    private Double latitude;
    private Double longitude;
    private Integer pageNum;
    private Integer pageSize;
    private String sortBy; // 为null表示默认排序
    private String order; // asc为升序，desc为降序
}
