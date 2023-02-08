package suzumiya.model.pojo;//package suzumiya.model.pojo;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.elasticsearch.common.geo.GeoPoint;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.*;
//import org.springframework.data.elasticsearch.core.suggest.Completion;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@Document(indexName = "hotel")
//@Data
//@NoArgsConstructor
//public class HotelDoc {
//
//    @Id
//    private Long id;
//    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", copyTo = "all")
//    private String name;
//    @Field(type = FieldType.Keyword)
//    private String address;
//    @Field(type = FieldType.Integer)
//    private Integer price;
//    @Field(type = FieldType.Integer)
//    private Integer score;
//    @Field(type = FieldType.Keyword, copyTo = "all")
//    private String brand;
//    @Field(type = FieldType.Keyword, copyTo = "all")
//    private String city;
//    @Field(type = FieldType.Keyword)
//    private String starName;
//    @Field(type = FieldType.Keyword, copyTo = "all")
//    private String business;
//    @GeoPointField
//    private GeoPoint location;
//    @Field(type = FieldType.Keyword, index = false)
//    private String pic;
//    @Field(type = FieldType.Boolean)
//    private Boolean isAD;
//
//    // 用于搜索的字段
//    @JsonIgnore
//    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart", ignoreFields = "all", excludeFromSource = true)
//    private String all;
//    // 自动补全字段
//    @JsonIgnore
//    @CompletionField(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", maxInputLength = 50)
//    private Completion suggestion;
//
//    // distance 在查询hotelDoc时会被赋值（单位：km）
//    private String distance;
//
//    public HotelDoc(Hotel hotel) {
//        this.id = hotel.getId();
//        this.name = hotel.getName();
//        this.address = hotel.getAddress();
//        this.price = hotel.getPrice();
//        this.score = hotel.getScore();
//        this.brand = hotel.getBrand();
//        this.city = hotel.getCity();
//        this.starName = hotel.getStarName();
//        this.business = hotel.getBusiness();
//        this.location = new GeoPoint(Double.parseDouble(hotel.getLatitude()), Double.parseDouble(hotel.getLongitude())); // geo_point
//        this.pic = hotel.getPic();
//        // 自动补全字段
//        List<String> t = new ArrayList<>();
//        t.add(this.brand); // 商标
//        String[] split = business.split("[(/)|(、)]");
//        t.addAll(Arrays.asList(split)); // 商圈
////        this.suggestion = new Completion(t.toArray(new String[t.size()]));
//        this.suggestion = new Completion(t.toArray(new String[t.size()]));
//    }
//}
