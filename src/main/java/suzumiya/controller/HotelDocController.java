package suzumiya.controller;//package suzumiya.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import suzumiya.model.dto.HotelSearchDTO;
//import suzumiya.model.vo.BaseResponse;
//import suzumiya.model.vo.HotelSearchVO;
//import suzumiya.util.ResponseGenerator;
//import suzumiya.service.IHotelDocService;
//
//@RestController
//@RequestMapping("/hotelDoc")
//public class HotelDocController {
//
//    @Autowired
//    private IHotelDocService hotelDocService;
//
//    @PostMapping
//    public BaseResponse<HotelSearchVO> search(@RequestBody HotelSearchDTO hotelSearchDTO) throws NoSuchFieldException, IllegalAccessException {
//        HotelSearchVO hotelSearchVO = hotelDocService.search(hotelSearchDTO);
//        return ResponseGenerator.returnOK("", hotelSearchVO);
//    }
//}
