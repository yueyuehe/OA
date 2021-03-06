package cn.hm.oil.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.hm.oil.domain.BasePipeline;
import cn.hm.oil.domain.BasePipelineSection;
import cn.hm.oil.domain.BasePipelineSpec;
import cn.hm.oil.domain.BaseReceiver;
import cn.hm.oil.domain.LoginUserInfo;
import cn.hm.oil.domain.Prompt;
import cn.hm.oil.domain.RunRecordcomprehensive;
import cn.hm.oil.domain.RunRecordcomprehensiveDetail;
import cn.hm.oil.domain.RunRecordcomprehensive_2016;
import cn.hm.oil.domain.SysUsers;
import cn.hm.oil.json.JsonResWrapper;
import cn.hm.oil.json.ResponseStatus;
import cn.hm.oil.service.BaseRcCompService_2016;
import cn.hm.oil.service.BaseService;
import cn.hm.oil.service.NewBaseService;
import cn.hm.oil.service.UserService;
import cn.hm.oil.util.CommonsMethod;
import cn.hm.oil.util.DateFormatter;
import cn.hm.oil.util.FileUtils;
import cn.hm.oil.util.JsonUtil;
import cn.hm.oil.util.PageSupport;
import cn.hm.oil.util.SettingUtils;

/**
 * @author Administrator
 * 
 *         阴极保护站运行月综合记录查看
 * 
 */
@Controller
@RequestMapping(value = "/admin/base/rc_comp/2016/")
public class BaseRcCompController_2016 {
	
	@Autowired
	private BaseRcCompService_2016 baseService;
	
	@Autowired
	private NewBaseService baseService_new;
	
	@Autowired
	private BaseService baseService_old;

	@Autowired
	private UserService userService;

	/**
	 * 阴极保护站运行月综合记录创建
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String rc_create(Model model,
			@RequestParam(required = false) String status) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		
		List<BasePipeline> pipeLineList = baseService.queryPipeLineByUser(ud.getId());

		model.addAttribute("pipeLineList", pipeLineList);
		List<BaseReceiver> br = baseService.queryLeaderList();
		model.addAttribute("br", br);
		
		Map<Integer, Integer> m = userService.getUsersRef();
		if (m != null && m.containsKey(ud.getId())) {
			model.addAttribute("up_id", m.get(ud.getId()));
		}
		return "pages/base/rc_comp_create_2016";
	}
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String plMeasure_save(Model model,
			@RequestParam(required = false) Integer rc_id,
			@RequestParam Integer pl_id,@RequestParam String name,
			@RequestParam String jinzhan, @RequestParam String r_month,
			@RequestParam String created_by, @RequestParam String auditor,
			@RequestParam String estimated, @RequestParam String actual,
			@RequestParam String wdtd, @RequestParam String jztd,
			@RequestParam String ljtd, @RequestParam String jcgxtd,
			@RequestParam String fljctd, @RequestParam String qttd,
			@RequestParam String o_max_a, @RequestParam String o_min_a,
			@RequestParam String o_avg_a, @RequestParam String o_max_v,
			@RequestParam String o_min_v, @RequestParam String o_avg_v,
			@RequestParam String tdd_v_max, @RequestParam String tdd_v_min,
			@RequestParam String sdl, @RequestParam String bhl,
			@RequestParam String up_id,
			@RequestParam String sbwhl, @RequestParam String comment,
			@RequestParam(required = false) Boolean all,
			@RequestParam(required = false) String s_pl_name,//搜索管线名称
			@RequestParam(required = false) Integer s_id,//搜索人的id
			@RequestParam(required = false) String s_user_name, // 搜索人的名字
			@RequestParam(required = false) Integer s_pl_id, //搜索管线id
			@RequestParam(required = false) String s_r_month, //
			@RequestParam(required = false) Integer modify, //是否是修改
			
			@RequestParam(required=false) String status) {		
		RunRecordcomprehensive_2016 rec = new RunRecordcomprehensive_2016();
		if(rc_id != null && rc_id.intValue() > 0)
			rec.setId(rc_id);
		rec.setName(name);
		rec.setAuditor(auditor);
		rec.setCreated_by(created_by);
		rec.setJinzhan(jinzhan);
		rec.setPl_id(pl_id);
		rec.setR_month(Integer.valueOf(r_month.replace(",", "")));
		rec.setStatus(Integer.parseInt(status));
		rec.setUp_id(up_id);
		LoginUserInfo ud = (LoginUserInfo) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		rec.setCreater(ud.getId());
		
		RunRecordcomprehensiveDetail recd = new RunRecordcomprehensiveDetail();
		recd.setEstimated(estimated);
		recd.setActual(actual);
		recd.setWdtd(wdtd);
		recd.setJztd(jztd);
		recd.setLjtd(ljtd);
		recd.setJcgxtd(jcgxtd);
		recd.setFljctd(fljctd);
		recd.setQttd(qttd);
		recd.setO_max_a(o_max_a);
		recd.setO_min_a(o_min_a);
		recd.setO_avg_a(o_avg_a);
		recd.setO_avg_v(o_avg_v);
		recd.setO_max_v(o_max_v);
		recd.setO_min_v(o_min_v);
		recd.setTdd_v_max(tdd_v_max);
		recd.setTdd_v_min(tdd_v_min);
		recd.setSdl(sdl);
		recd.setBhl(bhl);
		recd.setSbwhl(sbwhl);
		recd.setComment(comment);
		recd.setStatus(Integer.parseInt(status));

		if (recd != null) {			
			baseService.saveRcComp(rec, recd);
		}
		
		String params = "";
		if(modify != null && modify.intValue() == 1)
		{
			params = CommonsMethod.putUrlParam(params, "s_id", s_id);
			params = CommonsMethod.putUrlParam(params, "all", all);
			if(s_pl_id != null && s_pl_id.intValue() > 0)
				params = CommonsMethod.putUrlParam(params, "s_pl_id", s_pl_id);
			params = CommonsMethod.putUrlParam(params, "s_r_month", s_r_month);
			params = CommonsMethod.putUrlParam(params, "s_user_name", s_user_name);
			params = CommonsMethod.putUrlParam(params, "s_pl_name", s_pl_name);
		}
		
		return "redirect:/admin/base/rc_comp/2016/" +  ((modify != null && modify.intValue() == 1)?"show":"create") +params;
	}
	
	/**
	 * 查询阴极保护站运行月综合记录
	 * @param model
	 * @param request
	 * @param authentication
	 * @param pl 管线ID
	 * @param section 段落ID
	 * @param spec 规格ID
	 * @param r_month 月份
	 * @param del
	 * @return
	 */
	@RequestMapping(value = "/list", method = { RequestMethod.GET,RequestMethod.POST })
	public String rc_comp_list(Model model, HttpServletRequest request,
			Authentication authentication,			
			@RequestParam(required = false) Integer s_pl_id, // 管线
			@RequestParam(required = false) String s_pl_name, // 管线名称
			@RequestParam(required = false) String s_user_name, // 用户名字
			@RequestParam(required=false) Boolean all,
			@RequestParam(required = false) String s_r_month, // 时间
			
			@RequestParam(required = false) Integer verify) {
				
		LoginUserInfo ud = (LoginUserInfo) authentication.getPrincipal();
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> paramPipeline = new HashMap<String, Object>();
		Integer role = CommonsMethod.getDataByRole(authentication, userService, param, all);
		model.addAttribute("role", role);
		if(role==2)//技术工
		{
			if(verify != null && verify.intValue() == 1)
			{
				param.put("status", 0);
				paramPipeline.put("status", 0);
			}
			else{
				param.put("status", "-1,0,1");
				paramPipeline.put("status", "-1,0,1");
			}
			
			paramPipeline.put("up_id", ud.getId());
		}
		else if(role==3)//维护工		
		{			
			paramPipeline.put("user_id", ud.getId());
		}
		
		if(all != null)
		{
			model.addAttribute("all", all);
			paramPipeline.put("all", all);
		}
		
		if (s_pl_id != null && s_pl_id.intValue() > 0) {
			param.put("pl_id", s_pl_id);
			model.addAttribute("s_pl_id", s_pl_id);
		}		
		
		if (!StringUtils.isBlank(s_pl_name)) {
			param.put("name", s_pl_name);
			model.addAttribute("s_pl_name", s_pl_name);
		}
		
		if (!StringUtils.isBlank(s_user_name)) {
			param.put("user_name", s_user_name);
			model.addAttribute("s_user_name", s_user_name);
		}
		
		if (!StringUtils.isBlank(s_r_month)) {
			param.put("r_month", Integer.valueOf(s_r_month.replace("-", "")));
			model.addAttribute("s_r_month", s_r_month);
		}
		if(verify != null && verify.intValue() > 0)
		{
			param.put("verify", verify);
			model.addAttribute("verify", verify);
		}
		
		
		
		List<SysUsers> users = baseService.queryUsers(param);
		model.addAttribute("users", users);
		
		//管线 下拉列表
		List<BasePipeline> pipeLineList = baseService.queryPipeLineByAdminRcComp(paramPipeline);
		model.addAttribute("pipeLineList", pipeLineList);		
		
		return "pages/base/rc_comp_list_2016";
	}
	
	/**
	 * 阴极保护站运行月综合记录详情
	 * @param model
	 * @param id 管道ID
	 * @param offset 分页偏移
	 * @param tips_id
	 * @return
	 */
	@RequestMapping(value = "/show", method = {RequestMethod.GET,RequestMethod.POST})
	public String plPatrol_show(Model model,Authentication authentication,HttpServletRequest request,
								@RequestParam(required=false) Boolean all,
								@RequestParam(required = false) String s_r_month,
								@RequestParam(required = false) Integer s_id,
								@RequestParam(required = false) Integer verify,
								@RequestParam(required = false) Integer s_pl_id, // 管线
								@RequestParam(required = false) String s_pl_name, // 管线名称
								@RequestParam(required = false) String s_user_name // 用户名字
			) {
		LoginUserInfo ud = (LoginUserInfo) authentication.getPrincipal(); 
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, Object> paramPipeline = new HashMap<String, Object>();
		Integer role = CommonsMethod.getDataByRole(authentication, userService, param);
		model.addAttribute("role", role);
		if(role==2)//技术工
		{
			if(verify != null && verify.intValue() == 1)
			{
				param.put("status", 0);
				paramPipeline.put("status", 0);
			}
			else{
				param.put("status", "-1,0,1");
				paramPipeline.put("status", "-1,0,1");
			}
			
			paramPipeline.put("up_id", ud.getId());
		}
		else if(role==3)//维护工		
		{			
			paramPipeline.put("user_id", ud.getId());
		}
		
		if (!StringUtils.isBlank(s_r_month)) {
			param.put("r_month", Integer.valueOf(s_r_month.replace("-", "")));
			model.addAttribute("s_r_month", s_r_month);
		}
		
		if (s_pl_id != null && s_pl_id.intValue() > 0) {
			param.put("pl_id", s_pl_id);
			model.addAttribute("s_pl_id", s_pl_id);
		}		
		
		if (!StringUtils.isBlank(s_pl_name)) {
			param.put("name", s_pl_name);
			model.addAttribute("s_pl_name", s_pl_name);
		}
		
		if (!StringUtils.isBlank(s_user_name)) {
			param.put("user_name", s_user_name);
			model.addAttribute("s_user_name", s_user_name);
		}

		if(s_id != null && s_id.intValue() > 0)
		{
			param.put("user_id", s_id);
			model.addAttribute("s_id", s_id);
		}
		if(verify != null && verify.intValue() > 0)
		{
			param.put("verify", verify);
			model.addAttribute("verify", verify);
		}
		
		if(all != null)
		{
			model.addAttribute("all", all);
			paramPipeline.put("all", all);
		}
		
				
		List<BasePipeline> pipeLineList = baseService.queryPipeLineByAdminRcComp(paramPipeline);
		model.addAttribute("pipeLineList", pipeLineList);
		
		//获取用户角色ID
		{
			Map<String, Object> param1 = new HashMap<String,Object>(param);
			param1.put("limit", 10000);
			param1.put("offset", 0);
			param1.put("order", 1);
			List<RunRecordcomprehensive_2016> detailList1 = baseService.queryRcComps(param1, null);
			
			List<Integer> ppIdList = null;
			Object obj = request.getSession().getAttribute(ud.getId() + "ppIdList");
			if (obj != null) {
				ppIdList = (List<Integer>)obj;
			} else {
				request.getSession().removeAttribute(ud.getId() + "ppIdList");
				ppIdList = new ArrayList<Integer>();
			}
			if (!CollectionUtils.isEmpty(detailList1)) {
				for (RunRecordcomprehensive_2016 ppd : detailList1) {
					if (!ppIdList.contains(ppd.getId())) {
						ppIdList.add(ppd.getId());
					}
				}
				request.getSession().setAttribute(ud.getId() + "ppIdList", ppIdList);
			}
		}
				
		{
			PageSupport ps = PageSupport.initPageSupport(request);
			ps.setPageSize(1);
			List<RunRecordcomprehensive_2016> rcList = baseService.queryRcComps(param, ps);
			if(rcList==null || rcList.size() == 0){
				return "pages/base/rc_comp_show_2016";
			}
			RunRecordcomprehensive_2016 rc = rcList.get(0);
			rc.resetCanEidt(role);
			model.addAttribute("rc", rc);
			model.addAttribute("rcs", rcList);
			RunRecordcomprehensiveDetail rcd = baseService.queryRunRecordcomprehensiveDetailById(rc.getId());
			rcd.resetCanEidt(role);
			model.addAttribute("rcd", rcd);
		}
		
		return "pages/base/rc_comp_show_2016";
	}

	/**
	 * 阴极保护站运行月综合记录导出
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/exp", method = RequestMethod.GET)
	public String plMeasure_export(Model model, HttpServletRequest request,
			HttpServletResponse response,Authentication authentication,
			@RequestParam(required = false) Integer s_pl_id, 
			@RequestParam(required = false) String s_r_month,			
			@RequestParam(required = false) Integer s_id,//人员ID
			@RequestParam(required = false) Integer verify,
			@RequestParam(required = false) Boolean all,
			@RequestParam(required = false) String s_pl_name, // 管线名称
			@RequestParam(required = false) String s_user_name // 用户名字
			) {
		Map<String, Object> param = new HashMap<String, Object>();
		Integer role = CommonsMethod.getDataByRole(authentication, userService, param);
		model.addAttribute("role", role);
		if (!StringUtils.isBlank(s_r_month)) {
			param.put("r_month", Integer.valueOf(s_r_month.replace("-", "")));
		}
		
		if (s_id != null && s_id.intValue() > 0) {
			param.put("user_id", s_id);
		}
		
		if(all != null)
		{
			param.put("all", all);
		}
		
		if (s_pl_id != null && s_pl_id.intValue() > 0) {
			param.put("pl_id", s_pl_id);
		}		
		
		if (!StringUtils.isBlank(s_pl_name)) {
			param.put("name", s_pl_name);
		}
		
		if (!StringUtils.isBlank(s_user_name)) {
			param.put("user_name", s_user_name);
		}
		
		List<RunRecordcomprehensive_2016> rcList = baseService.queryRcComps(param, null);	
		
		if (rcList.size() == 0) {
			return "redirect:/admin/base/rc_comp/2016/list";
		}		
		
		String sep = System.getProperty("file.separator");
		String fileDir = SettingUtils.getCommonSetting("upload.file.temp.path");// 存放文件文件夹名称
		String path = fileDir;
		String excelPath = path + sep + "excel" + ".xls";
		File dirPath = new File(path);
		if (!dirPath.exists()) {
			dirPath.mkdirs();
		}
		try {
			HSSFWorkbook work = new HSSFWorkbook();
			FileOutputStream fileOut = new FileOutputStream(path + sep
					+ "excel" + ".xls");

			HSSFSheet sheet1 = work.createSheet("sheet1");
			HSSFCell cell;
			
			// 标题格式
			CellStyle cellStyle = work.createCellStyle();
			CellStyle rightStyle = work.createCellStyle();
			// 表头格式
			CellStyle titleStyle = work.createCellStyle();
			// 内容格式
			CellStyle dataStyle = work.createCellStyle();

			// 内容加上边框
			dataStyle.setBorderBottom(CellStyle.BORDER_THIN);
			// dataStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			dataStyle.setBorderLeft(CellStyle.BORDER_THIN);
			// dataStyle.setLeftBorderColor(IndexedColors.GREEN.getIndex());
			dataStyle.setBorderRight(CellStyle.BORDER_THIN);
			// dataStyle.setRightBorderColor(IndexedColors.BLUE.getIndex());
			dataStyle.setBorderTop(CellStyle.BORDER_THIN);
			// dataStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

			
			cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
			cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

			// 居右
			rightStyle.setAlignment(CellStyle.ALIGN_RIGHT);
			rightStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			rightStyle.setWrapText(true);
			// 居左
			titleStyle.setAlignment(CellStyle.ALIGN_LEFT);
			titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			titleStyle.setWrapText(true);

			dataStyle.setAlignment(CellStyle.ALIGN_CENTER);
			dataStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			dataStyle.setWrapText(true);

			// 标题字体
			Font font = work.createFont();
			// 表头字体
			Font titlefont = work.createFont();
			// 内容字体
			Font datafont = work.createFont();

			font.setFontHeightInPoints((short) 20);
			font.setFontName("方正大黑简体");
			//font.setBoldweight(Font.BOLDWEIGHT_BOLD);

			titlefont.setFontHeightInPoints((short) 10);
			titlefont.setFontName("方正仿宋简体");
			titlefont.setBoldweight(Font.BOLDWEIGHT_BOLD);

			datafont.setFontHeightInPoints((short) 10);
			datafont.setFontName("方正仿宋简体");
			datafont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			// datafont.setColor(HSSFColor.RED.index);

			// 把字体加入到格式中
			cellStyle.setFont(font);
			rightStyle.setFont(titlefont);
			titleStyle.setFont(titlefont);
			dataStyle.setFont(datafont);

			// 设置列宽度
			sheet1.setColumnWidth(0, 18 * 256);
			sheet1.setColumnWidth(1, 10 * 256);
			sheet1.setColumnWidth(2, 18 * 256);
			sheet1.setColumnWidth(3, 10 * 256);
			sheet1.setColumnWidth(4, 18 * 256);
			sheet1.setColumnWidth(5, 10 * 256);

			int row_index = 0;
			for (RunRecordcomprehensive_2016 rc : rcList) {
				// 合并单元格
				sheet1.addMergedRegion(new CellRangeAddress(row_index, row_index, 0, 5));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+1, row_index+1, 0, 2));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+1, row_index+1, 4, 5));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+2, row_index+2, 2, 3));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+8, row_index+8, 2, 3));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+5, row_index+5, 0, 5));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+9, row_index+9, 0, 5));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+11, row_index+11, 0, 5));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+11, row_index+11, 0, 5));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+12, row_index+12, 0, 1));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+12, row_index+12, 4, 5));
				sheet1.addMergedRegion(new CellRangeAddress(row_index+13, row_index+13, 0, 5));

				// 第一行
				HSSFRow row = sheet1.createRow(row_index);
				row.setHeightInPoints((float)45.00);
				row_index++;
				// 新建单元格
				cell = row.createCell(0);
				// 设置内容
				cell.setCellValue("阴极保护站运行月综合记录");
				// 设置单元格格式
				cell.setCellStyle(cellStyle);
				// 设置单元格内容类型
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				// 第二行
				row = sheet1.createRow(row_index);
				row_index++;
				// 设置行高度
				row.setHeightInPoints((float) 28.5);
				cell = row.createCell(0);
				cell.setCellValue("井(站) " + rc.getJinzhan());
				cell.setCellStyle(titleStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.createCell(4);
				cell.setCellValue(rc.getR_month().toString().substring(0, 4)
						+ " 年 " + rc.getR_month().toString().substring(4, rc.getR_month().toString().length())
						+ " 月 ");
				cell.setCellStyle(rightStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				int end_row = 0;
				// 添加边框线
				for (int rown = 0; rown < 10; rown++) {
					row = sheet1.createRow(rown+row_index);
					for (int celln = 0; celln < 6; celln++) {
						cell = row.createCell(celln);
						cell.setCellStyle(dataStyle);
					}
					end_row = row_index + rown;
				}

				// 第三行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(48);
				cell = row.getCell(0);
				cell.setCellValue("应送电时间 (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				RunRecordcomprehensiveDetail rcd = baseService.queryRunRecordcomprehensiveDetailById(rc.getId());

				cell = row.getCell(1);
				if(rcd.getEstimated()!=null) {
				cell.setCellValue(rcd.getEstimated());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(4);
				cell.setCellValue("实送电   (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(5);
				if(rcd.getActual()!=null) {
					cell.setCellValue(rcd.getActual());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				// 第四行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(48);
				cell = row.getCell(0);
				cell.setCellValue("外电停电 (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(1);
				if(rcd.getWdtd()!=null) {
					cell.setCellValue(rcd.getWdtd());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(2);
				cell.setCellValue("雷击停电 (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(3);
				if(rcd.getLjtd() != null) {
					cell.setCellValue(rcd.getLjtd());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(4);
				cell.setCellValue("机障停电 (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(5);
				if(rcd.getJztd() != null) {
					cell.setCellValue(rcd.getJztd());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				// 第五行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(48);
				cell = row.getCell(0);
				cell.setCellValue("防雷检查停电 (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(1);
				if(rcd.getFljctd() != null) {
					cell.setCellValue(rcd.getFljctd());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(2);
				cell.setCellValue("检测管线停电 (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(3);
				if(rcd.getJcgxtd() != null) {
					cell.setCellValue(rcd.getJcgxtd());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(4);
				cell.setCellValue("其他停电 (h)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(5);
				if(rcd.getQttd() != null) {
					cell.setCellValue(rcd.getQttd());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				// 第六行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints((float) 4.5);

				// 第七行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(48);
				cell = row.getCell(0);
				cell.setCellValue("输出最高 (A)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(1);
				if(rcd.getO_max_a() != null) {
					cell.setCellValue(rcd.getO_max_a());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(2);
				cell.setCellValue("输出最低 (A)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(3);
				if(rcd.getO_min_a() != null) {
					cell.setCellValue(rcd.getO_min_a());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(4);
				cell.setCellValue("月平均 (A)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(5);
				if(rcd.getO_avg_a() != null) {
					cell.setCellValue(rcd.getO_avg_a());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				// 第八行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(48);
				cell = row.getCell(0);
				cell.setCellValue("输出最高 (V)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(1);
				if(rcd.getO_max_v() != null) {
					cell.setCellValue(rcd.getO_max_v());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(2);
				cell.setCellValue("输出最低 (V)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(3);
				if(rcd.getO_min_v() != null) {
					cell.setCellValue(rcd.getO_min_v());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(4);
				cell.setCellValue("月平均 (V)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(5);
				if(rcd.getO_avg_v() != null) {
					cell.setCellValue(rcd.getO_avg_v());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				// 第九行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(48);
				cell = row.getCell(0);
				cell.setCellValue("通电点最高 (V)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(1);
				if(rcd.getTdd_v_max() != null) {
					cell.setCellValue(rcd.getTdd_v_max());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(4);
				cell.setCellValue("通电点最低 (V)");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(5);
				System.out.println("cell is null | " + cell==null);
				cell.setCellValue(rcd.getTdd_v_min() + "");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				// 第十行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints((float) 12.00);

				// 第十一行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(48);
				cell = row.getCell(0);
				cell.setCellValue("送电率 （%）");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(1);
				if(rcd.getSdl() != null) {
					cell.setCellValue(rcd.getSdl());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(2);
				cell.setCellValue("保护率 （%）");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(3);
				if(rcd.getBhl() != null) {
					cell.setCellValue(rcd.getBhl());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				cell = row.getCell(4);
				cell.setCellValue("设备完好率 （%）");
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.getCell(5);
				if(rcd.getSbwhl() != null) {
					cell.setCellValue(rcd.getSbwhl());
				}
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

				// 第十二行
				row = sheet1.getRow(row_index);
				row_index++;
				row.setHeightInPoints(264);
				cell = row.getCell(0);
				cell.setCellValue("运行分析:" + rcd.getComment());
				CellStyle commentStyle = work.createCellStyle();
				commentStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
				commentStyle.setWrapText(true);
				commentStyle.setFont(titlefont);
				commentStyle.setBorderLeft(CellStyle.BORDER_THIN);
				commentStyle.setBorderTop(CellStyle.BORDER_THIN);
				commentStyle.setBorderBottom(CellStyle.BORDER_THIN);
				cell.setCellStyle(commentStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				// 第十三行
				row = sheet1.createRow(row_index);
				row_index++;
				row.setHeightInPoints((float)23.25);
				cell = row.createCell(0);
				cell.setCellValue("填报人: " + rc.getCreated_by());
				cell.setCellStyle(titleStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				cell = row.createCell(4);
				cell.setCellValue("审核人: " + rc.getAuditor());
				cell.setCellStyle(rightStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);

				row = sheet1.createRow(row_index);
				row.setHeightInPoints((float) 93.75);
				for(int i = 0; i < 5; i++) {
					cell = row.createCell(i);
					cell.setCellStyle(titleStyle);
				}
				cell = row.getCell(0);
				cell.setCellValue("填写说明：1.站名后必须填写管道名称与所辖段落，若有多条线路时，须分开填写；运行分析中应阐述送电管道长度与绝缘层情况，出现异常时，要进行原因分析。填报人由油气管道保护工填写，审核人由技术干部或作业区防腐人员填写。2.无停电栏填写“0”不能由“/”表示。");
				
				row_index = end_row + 5;
			}
			// 将创建好的excel存到指定文件夹下
			work.write(fileOut);
			fileOut.close();
			// 压缩文件夹并下载，下载后删除文件夹
			FileUtils.createZip(response, excelPath, DateFormatter.dateToString(new Date(), "yyyy-MM-dd_HH:mm:ss:SSS"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(value = "/verify_save", method = RequestMethod.POST)
	public @ResponseBody String plMeasure_verify_save(Model model,HttpServletRequest request, 
			@RequestParam Integer id, @RequestParam Integer status,
			@RequestParam String verify_desc) {
		LoginUserInfo ud = (LoginUserInfo) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		
		Object obj = request.getSession().getAttribute(ud.getId() + "ppIdList");
		if (obj != null) {
			List<Integer> ppIdList = (List<Integer>)obj;
			for (Integer ppId : ppIdList)
				baseService.updateRunRecordcomprehensiveVerifyStatus(ppId, ud.getId(), status, verify_desc);
			request.getSession().removeAttribute(ud.getId() + "ppIdList");
		}
		/*if(status!=null){
			RunRecordcomprehensive p = baseRunRecordcomprehensiveDao.queryRunRecordcomprehensiveById(id);
			String content;
			if (status.intValue() == 1) {
				content = "您提交的阴极保护站运行月综合记录已审核通过！";
			} else {
				content = "您提交的阴极保护站运行月综合记录未审核通过！";
			}
				saveTips(content, p.getCreater(), "/admin/base/rc_comp/show?id=" + id);
				//eventDao.updateEventStatus(status, message, event_id, type_id);
			}*/
		return JsonUtil.toJson("OK");
	}	
}
