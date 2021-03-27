package cn.itrip.controller;

import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.comment.ItripScoreCommentVO;
import cn.itrip.beans.vo.comment.ItripSearchCommentVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.Page;
import cn.itrip.common.SystemConfig;
import cn.itrip.common.ValidationToken;
import cn.itrip.service.comment.ItripCommentService;
import cn.itrip.service.hotel.ItripHotelService;
import cn.itrip.service.image.ItripImageService;
import cn.itrip.service.labeldic.ItripLabelDicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论Controller
 *
 * 包括API接口：
 * 1、根据type 和target id 查询评论照片
 * 2、据酒店id查询酒店平均分（总体评分、位置评分、设施评分、服务评分、卫生评分）
 * 3、根据酒店id查询评论数量
 * 4、根据评论类型查询评论 分页
 * 5、上传评论图片
 * 6、删除评论图片
 * 7、新增评论信息
 * 8、查看个人评论信息
 * 9、查询出游类型列表
 * 10、新增评论信息页面获取酒店相关信息（酒店名称、酒店图片、酒店星级）
 *
 * 注：错误码（100001 ——100100）
 *
 * Created by hanlu on 2017/5/9.
 */
@Controller
@Api(value = "API", basePath = "/http://api.itrap.com/api")
@RequestMapping(value="/api/comment")
public class SystemCommentController {
	private Logger logger = Logger.getLogger(SystemCommentController.class);

	@Resource
	private SystemConfig systemConfig;

	@Resource
	private ItripCommentService itripCommentService;

	@Resource
	private ValidationToken validationToken;

	@Resource
	private ItripImageService itripImageService;

	@Resource
	private ItripLabelDicService itripLabelDicService;

	@Resource
    private ItripHotelService itripHotelService;

	/**
	 * 根据酒店id查询酒店平均分
	 * @param hotelId
	 * @return
	 */

    @RequestMapping(value = "/gethotelscore/{hotelId}",method=RequestMethod.GET,produces = "application/json")
    @ResponseBody
    public Dto<Object> getHotelScore(@ApiParam(required = true, name = "hotelId", value = "酒店ID")
										 @PathVariable String hotelId){
        Dto<Object> dto = new Dto<Object>();
        logger.debug("getHotelScore hotelId : " + hotelId);
        if(null != hotelId && !"".equals(hotelId)){
            ItripScoreCommentVO itripScoreCommentVO = new ItripScoreCommentVO();
            try {
                itripScoreCommentVO =  itripCommentService.getAvgAndTotalScore(Long.valueOf(hotelId));
				dto = DtoUtil.returnSuccess("获取评分成功",itripScoreCommentVO);
            } catch (Exception e) {
                e.printStackTrace();
				dto = DtoUtil.returnFail("获取评分失败","100001");
            }
        }else{
			dto = DtoUtil.returnFail("hotelId不能为空","100002");
        }
        return dto;
    }


	/**
	 * 根据酒店id查询各类评论数量
	 * @param hotelId
	 * @return
	 */
	@RequestMapping(value = "/getcount/{hotelId}",method=RequestMethod.GET,produces = "application/json")
	@ResponseBody
	public Dto<Object> getCommentCountByType(@ApiParam(required = true, name = "hotelId", value = "酒店ID")
											 @PathVariable String hotelId){
		Dto<Object> dto = new Dto<Object>();
		logger.debug("hotelId ================= " + hotelId);
		Integer count = 0;
		Map<String,Integer> countMap = new HashMap<String,Integer>();
		Map<String,Object> param = new HashMap<String,Object>();
		if(null != hotelId && !"".equals(hotelId)){
			param.put("hotelId",hotelId);
			count = getItripCommentCountByMap(param);
			if(count != -1){
				countMap.put("allcomment",count);
			}else{
				return DtoUtil.returnFail("获取酒店总评论数失败","100014");
			}
			param.put("isOk",1);//0：有待改善 1：值得推荐
			count = getItripCommentCountByMap(param);
			if(count != -1){
				countMap.put("isok",count);
			}else{
				return DtoUtil.returnFail("获取酒店值得推荐评论数失败","100017");
			}
			param.put("isOk",0);//0：有待改善 1：值得推荐
			count = getItripCommentCountByMap(param);
			if(count != -1){
				countMap.put("improve",count);
			}else{
				return DtoUtil.returnFail("获取酒店有待改善评论数失败","100016");
			}
			param.put("isHavingImg",1);//0:无图片 1:有图片
			param.put("isOk",null);
			count = getItripCommentCountByMap(param);
			if(count != -1){
				countMap.put("havingimg",count);
			}else{
				return DtoUtil.returnFail("获取酒店有图片评论数失败","100015");
			}
		}else{
			return DtoUtil.returnFail("参数hotelId为空","100018");
		}
		dto = DtoUtil.returnSuccess("获取酒店各类评论数成功",countMap);
		return dto;
	}

	public Integer getItripCommentCountByMap(Map<String,Object> param){
		Integer count  = -1;
		try {
			count =  itripCommentService.getItripCommentCountByMap(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * 根据评论类型查询评论列表，并分页显示
	 * @param itripSearchCommentVO
	 * @return
	 */
	@RequestMapping(value = "/getcommentlist",method=RequestMethod.POST,produces = "application/json")
	@ResponseBody
	public Dto<Object> getCommentList(@RequestBody ItripSearchCommentVO itripSearchCommentVO){
		Dto<Object> dto = new Dto<Object>();
		Map<String,Object> param=new HashMap<>();
		logger.debug("hotelId : " + itripSearchCommentVO.getHotelId());
		logger.debug("isHavingImg : " + itripSearchCommentVO.getIsHavingImg());
		logger.debug("isOk : " + itripSearchCommentVO.getIsOk());
		if(itripSearchCommentVO.getIsOk() == -1){
			itripSearchCommentVO.setIsOk(null);
		}
		if(itripSearchCommentVO.getIsHavingImg() == -1){
			itripSearchCommentVO.setIsHavingImg(null);
		}
		param.put("hotelId",itripSearchCommentVO.getHotelId());
		param.put("isHavingImg",itripSearchCommentVO.getIsHavingImg());
		param.put("isOk",itripSearchCommentVO.getIsOk());
		try{
			Page page = itripCommentService.queryItripCommentPageByMap(param,
					itripSearchCommentVO.getPageNo(),
					itripSearchCommentVO.getPageSize());
			dto = DtoUtil.returnDataSuccess(page);
		}catch (Exception e){
			e.printStackTrace();
			dto = DtoUtil.returnFail("获取评论列表错误","100020");
		}

		return dto;
	}
}



