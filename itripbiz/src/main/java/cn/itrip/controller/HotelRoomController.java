package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.ItripImageVO;
import cn.itrip.beans.vo.ItripLabelDicVO;
import cn.itrip.beans.vo.hotelroom.ItripHotelRoomVO;
import cn.itrip.beans.vo.hotelroom.SearchHotelRoomVO;
import cn.itrip.common.DateUtil;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.EmptyUtils;
import cn.itrip.service.hotelroom.ItripHotelRoomService;
import cn.itrip.service.image.ItripImageService;
import cn.itrip.service.labeldic.ItripLabelDicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@Api(value = "API", basePath = "/http://api.itrap.com/api")
@RequestMapping(value = "/api/hotelroom")
public class HotelRoomController {

    @Resource
    private ItripImageService itripImageService;

    @Resource
    private ItripHotelRoomService itripHotelRoomService;

    @Resource
    private ItripLabelDicService itripLabelDicService;
    @RequestMapping(value = "/queryhotelroombyhotel", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Dto<List<ItripHotelRoomVO>> queryHotelRoomByHotel(@RequestBody SearchHotelRoomVO vo) {
        List<List<ItripHotelRoomVO>> hotelRoomVOList = null;
        try {
            Map<String, Object> param = new HashMap();
            if (EmptyUtils.isEmpty(vo.getHotelId())) {
                return DtoUtil.returnFail("酒店ID不能为空", "100303");
            }
            if (EmptyUtils.isEmpty(vo.getStartDate()) || EmptyUtils.isEmpty(vo.getEndDate())) {
                return DtoUtil.returnFail("必须填写酒店入住及退房时间", "100303");
            }
            if (EmptyUtils.isNotEmpty(vo.getStartDate()) && EmptyUtils.isNotEmpty(vo.getEndDate())) {
                if (vo.getStartDate().getTime() > vo.getEndDate().getTime()) {
                    return DtoUtil.returnFail("入住时间不能大于退房时间", "100303");
                }
                List<Date> dates = DateUtil.getBetweenDates(vo.getStartDate(), vo.getEndDate());
                param.put("timesList", dates);
            }

            vo.setIsHavingBreakfast(EmptyUtils.isEmpty(vo.getIsHavingBreakfast()) ? null : vo.getIsHavingBreakfast());
            vo.setIsBook(EmptyUtils.isEmpty(vo.getIsBook()) ? null : vo.getIsBook());
            vo.setIsTimelyResponse(EmptyUtils.isEmpty(vo.getIsTimelyResponse()) ? null : vo.getIsTimelyResponse());
            vo.setRoomBedTypeId(EmptyUtils.isEmpty(vo.getRoomBedTypeId()) ? null : vo.getRoomBedTypeId());
            vo.setIsCancel(EmptyUtils.isEmpty(vo.getIsCancel()) ? null : vo.getIsCancel());
            vo.setPayType(EmptyUtils.isEmpty(vo.getPayType()) ? null : vo.getPayType());

            param.put("hotelId", vo.getHotelId());
            param.put("isBook", vo.getIsBook());
            param.put("isHavingBreakfast", vo.getIsHavingBreakfast());
            param.put("isTimelyResponse", vo.getIsTimelyResponse());
            param.put("roomBedTypeId", vo.getRoomBedTypeId());
            param.put("isCancel", vo.getIsCancel());
            //param.put("payType", vo.getPayType()==3?null:vo.getPayType());
            if(vo.getPayType() != null && vo.getPayType() == 3){
                param.put("payType", null);
            }else{
                param.put("payType", vo.getPayType());
            }

            List<ItripHotelRoomVO> originalRoomList = itripHotelRoomService.getItripHotelRoomListByMap(param);
            hotelRoomVOList = new ArrayList();
            for (ItripHotelRoomVO roomVO : originalRoomList) {
                List<ItripHotelRoomVO> tempList = new ArrayList<ItripHotelRoomVO>();
                tempList.add(roomVO);
                hotelRoomVOList.add(tempList);
            }
            return DtoUtil.returnSuccess("获取成功", hotelRoomVOList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取酒店房型列表失败", "100304");
        }
    }
    @RequestMapping(value = "/queryhotelroombed", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dto<Object> queryHotelRoomBed() {
        try {
            List<ItripLabelDicVO> itripLabelDicList = itripLabelDicService.getItripLabelDicByParentId(new Long(1));
            return DtoUtil.returnSuccess("获取成功", itripLabelDicList);
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail("获取床型失败", "100305");
        }
    }
    @RequestMapping(value = "/getimg/{targetId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Dto<Object> getImgByTargetId(@ApiParam(required = true, name = "targetId", value = "酒店房型ID") @PathVariable String targetId) {
        Dto<Object> dto = null;
        if (null != targetId && !"".equals(targetId)) {
            List<ItripImageVO> itripImageVOList = null;
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("type", "1");
            param.put("targetId", targetId);
            try {
                itripImageVOList = itripImageService.getItripImageListByMap(param);
                dto = DtoUtil.returnSuccess("获取酒店图片房型成功", itripImageVOList);
            } catch (Exception e) {
                e.printStackTrace();
                dto = DtoUtil.returnFail("获取酒店房型图片失败", "100301");
            }
        } else {
            dto = DtoUtil.returnFail("酒店房型id不能为空", "100302");
        }
        return dto;
    }
}
