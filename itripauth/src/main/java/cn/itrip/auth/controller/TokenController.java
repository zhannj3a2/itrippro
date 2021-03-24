package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.DtoUtil;
import cn.itrip.common.ErrorCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

//import cn.itrip.auth.exception.TokenValidationFailedException;

/**
 * Token控制器
 * @author hduser
 *
 */
@Controller
@RequestMapping(value = "/api")
public class TokenController {

	@Resource
	private TokenService tokenService;

	@RequestMapping(value = "/validateToken",method =RequestMethod.GET, produces = "application/json",headers = "token")
	public @ResponseBody
	Dto validate(HttpServletRequest request) {
		try {
			boolean result=tokenService.validate(request.getHeader("token"),request.getHeader("user-agent"));
			if(result){
				return DtoUtil.returnSuccess("token有效");
			}else{
				return DtoUtil.returnSuccess("token无效");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
		}

	}
	/**
	 * 置换token
	 *
	 * @return 新的token信息
	 */

	@RequestMapping(value = "/retoken", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Dto replace(HttpServletRequest request) {
		String token;
		try {
			token=tokenService.reloadToken(request.getHeader("user-agent"),request.getHeader("token"));
			ItripTokenVO vo=new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis()+2*60*60*1000
			, Calendar.getInstance().getTimeInMillis());
			return  DtoUtil.returnDataSuccess(vo);
		} catch (Exception e) {
			e.printStackTrace();
			return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_UNKNOWN);
		}

	}
}

