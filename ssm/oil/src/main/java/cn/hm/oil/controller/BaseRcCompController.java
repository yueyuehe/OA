package cn.hm.oil.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import cn.hm.oil.domain.SysUsers;
import cn.hm.oil.json.JsonResWrapper;
import cn.hm.oil.json.ResponseStatus;
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
@RequestMapping(value = "/admin/base")
public class BaseRcCompController {

	@Autowired
	private BaseService baseService;

	@Autowired
	private UserService userService;

	/**
	 * 阴极保护站运行月综合记录创建
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/rc_comp/create", method = RequestMethod.GET)
	public String rc_create(Model model,
			@RequestParam(required = false) String status) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		List<BasePipeline> pipeLineList = baseService.queryPipeLineByUser(ud
				.getId());

		model.addAttribute("pipeLineList", pipeLineList);
		List<BaseReceiver> br = baseService.queryLeaderList();
		model.addAttribute("br", br);
		Prompt prompt = baseService.quertPromptByType(4);
		if(prompt != null) {
			model.addAttribute("prompt", prompt);
		}
		if (StringUtils.equals(status, "1")) {
			model.addAttribute("msg", "保存成功！");
		} else if (StringUtils.equals(status, "0")) {
			model.addAttribute("msg", "保存失败！");
		}
		Map<Integer, Integer> m = userService.getUsersRef();
		if (m != null && m.containsKey(ud.getId())) {
			model.addAttribute("up_id", m.get(ud.getId()));
		}
		return "pages/base/rc_comp_create";
	}

	@RequestMapping(value="/rc_comp/getTime")
	public @ResponseBody JsonResWrapper GetTime(@RequestParam Integer id) {
		JsonResWrapper response = new JsonResWrapper();
		RunRecordcomprehensive pc = baseService.queryRunRecordcomprehensiveById(id);
		pc.setCreate_t(DateFormatter.dateToString(pc.getCreate_time(), "yyyy-MM-dd HH:mm:ss"));
		if(pc.getVerify_time() != null){
			pc.setVerify_t(DateFormatter.dateToString(pc.getVerify_time(), "yyyy-MM-dd HH:mm:ss"));
		}
		response.setStatus(ResponseStatus.OK);
		response.setData(pc);
		return response;
	}
	
	@RequestMapping(value = "/rc_comp/modify", method = RequestMethod.POST)
	public String plPatrol_modify(Model model, HttpServletRequest request,
			@RequestParam(required = false) Integer id,
			@RequestParam(required = false) Integer verify,
			@RequestParam(required=false) Integer tips_id,			
			
			@RequestParam Integer pl,
			@RequestParam Integer section, @RequestParam Integer spec,
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
			@RequestParam String sbwhl, @RequestParam String comment
			)
			//@RequestParam(required = false) String up_id,@RequestParam(required = false) String status)
	{
		RunRecordcomprehensive rec = new RunRecordcomprehensive();
		rec.setId(id);
		rec.setAuditor(auditor);
		rec.setCreated_by(created_by);
		rec.setJinzhan(jinzhan);
		rec.setPl_id(pl);
		rec.setPl_section_id(section);
		rec.setPl_spec_id(spec);
		rec.setR_month(Integer.valueOf(r_month.replace(",", "")));
		rec.setStatus(0);
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
		
		/*
		
		
		
		
		int i = 0;
		for (Integer pId : pmd_id)
		{
			if(StringUtil.isBlank(no[i]) == false)
			{
				PotentialMeasureDetail pmd = new PotentialMeasureDetail();
				pmd.setM_date(DateFormatter.stringToDate(m_date[i],
						"yyyy-MM-dd"));
				pmd.setPotential(potential[i]);
				pmd.setMeasurer(measurer[i]);
				pmd.setRemark(remark[i]);
				pmd.setNo(no[i]);
				pmd.setId(pId);
				pmd.setStatus(new Integer(-2));	
				newBaseService.modifyPlMeasureDetail(pmd);
			}
			++i;
		}		
		
*/
		String page = "";
		if(id!=null && id.intValue() > 0)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			else
				page += "&";
			page += "id="+id.toString();
		}
		if(r_month!=null && StringUtil.isBlank(r_month)==false)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			else
				page += "&";
			page += "r_month="+r_month;
		}
		if(verify!=null)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			else
				page += "&";
			page += "verify="+verify.toString();
		}
		if(tips_id!=null)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			else
				page += "&";
			page += "tips_id="+tips_id.toString();
		}
		
		return "redirect:/admin/base/new/pl_measure/show"+page;//"pages/base/pl_patrol_show_new";
	}
	
	/**
	 * 管道巡检工作记录批量更改状态
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/rc_comp/batch_changestatus", method = {RequestMethod.GET,RequestMethod.POST})
	public String plPatrol_batch_changestatus(Authentication authentication,HttpServletRequest request,
							  @RequestParam(required = false) Integer id, 
							  @RequestParam(required = false) String r_month,
							  @RequestParam(required = false) Integer verify,
							  @RequestParam(required = false) Integer[] oldStatus,
							  @RequestParam(required = false) Integer newStatus,
							  @RequestParam(required=false) Integer tips_id) {
		
		if (tips_id != null && tips_id.intValue() > 0){
			baseService.deleteTips(tips_id);
		}
		
		Map<String, Object> param = new HashMap<String, Object>();

		CommonsMethod.getDataByRole(authentication, userService, param);
		if(!StringUtils.isBlank(r_month)){
			param.put("r_month", r_month);
		}
		if(id != null)
		{
			param.put("id", id);
		}
		/*
		CommonsMethod.getDataByRole(authentication, userService, param);
		PerformanceMeasure pm = newBaseService.queryValvePatrolByList(param);
		
		if(pm!=null)
		{
			param.put("pl", pm.getPl_id());
			param.put("spec", pm.getPl_spec_id());
			param.put("section", pm.getPl_section_id());
			List<PotentialMeasureDetail> pmd = newBaseService.queryPotentialMeasureDetail(param);
			for(PotentialMeasureDetail p : pmd)
			{
				if(Arrays.asList(oldStatus).contains(p.getStatus()))
				{
					newBaseService.updateChangePotentialMeasureDetailStatus(new Integer(p.getId()), newStatus);   
				}
			}
		}
		*/
		String page = "";
		if(id!=null && id.intValue() > 0)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			page += "id="+id.toString();
		}
		if(r_month!=null && StringUtil.isBlank(r_month)==false)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			page += "r_month="+r_month;
		}
		if(verify!=null)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			page += "verify="+verify.toString();
		}
		if(tips_id!=null)
		{
			if(page.isEmpty())
				page += ".jhtml?";
			page += "tips_id="+tips_id.toString();
		}
		return "redirect:/admin/base/new/pl_measure/show"+page;
	}
	
	@RequestMapping(value="/rc_comp/setTime")
	public @ResponseBody JsonResWrapper setTime(@RequestParam Integer id,@RequestParam String create_t,@RequestParam String verify_t) {
		JsonResWrapper response = new JsonResWrapper();
		baseService.updateRunRecordcomprehensiveTime(id, create_t, verify_t);
		response.setStatus(ResponseStatus.OK);
		return response;
	}
	
	
	@RequestMapping(value = "/rc_comp/save", method = RequestMethod.POST)
	public String plMeasure_save(Model model,
			@RequestParam(required = false) Integer id,
			@RequestParam Integer pl,
			@RequestParam Integer section, @RequestParam Integer spec,
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
			@RequestParam String sbwhl, @RequestParam String comment) {

		RunRecordcomprehensive rec = new RunRecordcomprehensive();
		rec.setId(id);
		rec.setAuditor(auditor);
		rec.setCreated_by(created_by);
		rec.setJinzhan(jinzhan);
		rec.setPl_id(pl);
		rec.setPl_section_id(section);
		rec.setPl_spec_id(spec);
		rec.setR_month(Integer.valueOf(r_month.replace(",", "")));
		rec.setStatus(0);
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
		
		String status = "0";
		if (recd != null) {
			
			baseService.saveRcComp(rec, recd);
			status = "1";
		}
		String page = "create" + "?status=" + status;
		if (id != null && id.intValue() > 0) {
			page = "edit?id=" + id + "&status=" + status;
		}
		
		return "redirect:/admin/base/rc_comp/"+page;
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
	@RequestMapping(value = "/rc_comp/list", method = { RequestMethod.GET,RequestMethod.POST })
	public String rc_comp_list(Model model, HttpServletRequest request,
			Authentication authentication,
			@RequestParam(required = false) Integer pl,
			@RequestParam(required = false) Integer section,
			@RequestParam(required = false) Integer spec,
			@RequestParam(required = false) String r_month,
			@RequestParam(required = false) Integer del) {
		
		if (del != null && del.intValue() == 1) {
			model.addAttribute("msg", "删除成功！");
		}

		//规格查询条件
		Map<String, Object> param = new HashMap<String, Object>();
		
		if (pl != null && pl.intValue() > 0) {
			param.put("pl_id", pl);
			model.addAttribute("pl", pl);

			//段落 下拉列表
			List<BasePipelineSection> sectionList = baseService.querySection(pl);
			model.addAttribute("sectionList", sectionList);
		}
		if (section != null && section.intValue() > 0) {
			param.put("pl_section_id", section);
			model.addAttribute("section", section);

			//规格 下拉列表
			List<BasePipelineSpec> specs = baseService.querySpec(section);
			model.addAttribute("specs", specs);
		}
		if (spec != null && spec.intValue() > 0) {
			param.put("pl_spec_id", spec);
			model.addAttribute("spec", spec);
		}
		
		//判读用户是否是维护工，维护工只能查看自己的数据
		Integer role = CommonsMethod.getDataByRole(authentication, userService, param);
		model.addAttribute("role", role);
		
		//管线 下拉列表
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(param);
		model.addAttribute("pipeLineList", pipeLineList);
		//规格 显示列表
		List<BasePipelineSpec> specList = baseService.querySpecListNew(param);
		model.addAttribute("specLists", specList);

		
		
		return "pages/base/rc_comp_list";
		
		//----------------------------------------------------------------
		//----------------------------------------------------------------
		
		/*List<BasePipeline> pipeLineList = baseService.queryPipeLine();

		Map<String, Object> param = new HashMap<String, Object>();

		if (pl != null && pl.intValue() > 0) {
			param.put("pl_id", pl);
			model.addAttribute("pl", pl);

			List<BasePipelineSection> sectionList = baseService.querySection(pl);
			model.addAttribute("sectionList", sectionList);
		}
		if (section != null && section.intValue() > 0) {
			param.put("pl_section_id", section);
			model.addAttribute("section", section);

			List<BasePipelineSpec> specList = baseService.querySpec(section);
			model.addAttribute("specList", specList);
		}
		if (spec != null && spec.intValue() > 0) {
			param.put("pl_spec_id", spec);
			model.addAttribute("spec", spec);
		}

		if (!StringUtils.isBlank(r_month)) {
			param.put("r_month", Integer.valueOf(r_month.replace("-", "")));
			model.addAttribute("r_month", r_month);
		}

		PageSupport ps = PageSupport.initPageSupport(request);

		List<RunRecordcomprehensive> rcList = baseService
				.queryRunRecordcomprehensive(param, ps);
		LoginUserInfo ud = (LoginUserInfo) authentication.getPrincipal();
		Integer role = userService.queryRoleIdByUserId(ud.getId());
		if (del != null && del.intValue() == 1) {
			model.addAttribute("msg", "删除成功！");
		}
		model.addAttribute("role", role);

		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("rcList", rcList);
		return "pages/base/rc_comp_list";*/
	}
	
	/**
	 * 审核记录
	 * @param model
	 * @param request
	 * @param authentication
	 * @param pl 管线ID
	 * @param section 段落ID
	 * @param spec 规格ID
	 * @param r_month 月份
	 * @return
	 */
	@RequestMapping(value = "/rc_comp/verify", method = { RequestMethod.GET,RequestMethod.POST })
	public String rcRecord_verify(Model model, HttpServletRequest request,
			Authentication authentication,
			@RequestParam(required = false) Integer pl,
			@RequestParam(required = false) Integer section,
			@RequestParam(required = false) Integer spec,
			@RequestParam(required = false) String r_month) {
		
		//管线 下拉列表
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(new HashMap<String,Object>());

		Map<String, Object> param = new HashMap<String, Object>();

		if (pl != null && pl.intValue() > 0) {
			param.put("pl_id", pl);
			model.addAttribute("pl", pl);

			List<BasePipelineSection> sectionList = baseService.querySection(pl);
			model.addAttribute("sectionList", sectionList);
		}
		if (section != null && section.intValue() > 0) {
			param.put("pl_section_id", section);
			model.addAttribute("section", section);

			List<BasePipelineSpec> specList = baseService.querySpec(section);
			model.addAttribute("specList", specList);
		}
		if (spec != null && spec.intValue() > 0) {
			param.put("pl_spec_id", spec);
			model.addAttribute("spec", spec);
		}

		if (!StringUtils.isBlank(r_month)) {
			param.put("r_month", Integer.valueOf(r_month.replace("-", "")));
			model.addAttribute("r_month", r_month);
		}
		
		LoginUserInfo ud = (LoginUserInfo) authentication.getPrincipal();
		Integer role = userService.queryRoleIdByUserId(ud.getId());
		model.addAttribute("role", role);

		List<BasePipelineSpec> specList = baseService.queryRunRecordcomprehensiveAuditSpecList(param);

		model.addAttribute("verify", 1);
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("specList", specList);
		
		return "pages/base/rc_comp_list";
	}
	/*@RequestMapping(value = "/rc_comp/verify", method = { RequestMethod.GET,
			RequestMethod.POST })
	public String rcRecord_verify(Model model, HttpServletRequest request,
			Authentication authentication,
			@RequestParam(required = false) Integer pl,
			@RequestParam(required = false) Integer section,
			@RequestParam(required = false) Integer spec,
			@RequestParam(required = false) String r_month) {
		List<BasePipeline> pipeLineList = baseService.queryPipeLine();

		Map<String, Object> param = new HashMap<String, Object>();

		if (pl != null && pl.intValue() > 0) {
			param.put("pl_id", pl);
			model.addAttribute("pl", pl);

			List<BasePipelineSection> sectionList = baseService
					.querySection(pl);
			model.addAttribute("sectionList", sectionList);
		}
		if (section != null && section.intValue() > 0) {
			param.put("pl_section_id", section);
			model.addAttribute("section", section);

			List<BasePipelineSpec> specList = baseService.querySpec(section);
			model.addAttribute("specList", specList);
		}
		if (spec != null && spec.intValue() > 0) {
			param.put("pl_spec_id", spec);
			model.addAttribute("spec", spec);
		}

		if (!StringUtils.isBlank(r_month)) {
			param.put("r_month", Integer.valueOf(r_month.replace("-", "")));
			model.addAttribute("r_month", r_month);
		}
		LoginUserInfo ud = (LoginUserInfo) authentication.getPrincipal();
		param.put("verify", "1");
		param.put("user_id", ud.getId());
		PageSupport ps = PageSupport.initPageSupport(request);

		List<RunRecordcomprehensive> rcList = baseService
				.queryRunRecordcomprehensive(param, ps);
		
		Integer role = userService.queryRoleIdByUserId(ud.getId());

		model.addAttribute("role", role);

		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("rcList", rcList);
		model.addAttribute("verify", 1);
		return "pages/base/rc_comp_list";
	}*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 阴极保护站运行月综合记录详情
	 * @param model
	 * @param id 管道ID
	 * @param offset 分页偏移
	 * @param tips_id
	 * @return
	 */
	@RequestMapping(value = "/rc_comp/show", method = RequestMethod.GET)
	public String plPatrol_show(Model model,Authentication authentication,
								@RequestParam Integer id, 
								@RequestParam(required = false) String r_month,
								@RequestParam(required = false) Integer verify,
								@RequestParam(required=false) Integer offset,
								@RequestParam(required=false) Integer tips_id) {
		
		if (tips_id != null && tips_id.intValue() > 0){
			baseService.deleteTips(tips_id);
		}
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("id", id);
		param.put("offset", offset);
		param.put("pageSize", 1);

		if(!StringUtils.isBlank(r_month)){
			param.put("r_month", Integer.valueOf(r_month.replace("-", "")));
			model.addAttribute("r_month", r_month);
		}
		
		/*RunRecordcomprehensiveDetail rcd = baseService.queryRunRecordcomprehensiveDetailById(id);
		RunRecordcomprehensive rc = baseService.queryRunRecordcomprehensiveById(id);*/
		
		RunRecordcomprehensive rc = baseService.queryRunRecordcomprehensiveByParam(param);
		int rcTotal = baseService.queryRunRecordcomprehensiveByParamTotal(param);
		if(rc==null){
			return "pages/base/rc_comp_show";
		}
		RunRecordcomprehensiveDetail rcd = baseService.queryRunRecordcomprehensiveDetailById(rc.getId());
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(new HashMap<String,Object>());
		List<BasePipelineSection> sectionList = baseService.querySection(rc.getPl_id());
		List<BasePipelineSpec> specList = baseService.querySpec(rc.getPl_section_id());

		//获取用户角色ID
		LoginUserInfo ud = (LoginUserInfo) authentication.getPrincipal();
		Integer role = userService.queryRoleIdByUserId(ud.getId());
		model.addAttribute("role", role);
		
		model.addAttribute("pl", rc.getPl_id());
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("section", rc.getPl_section_id());
		model.addAttribute("sectionList", sectionList);
		model.addAttribute("spec", rc.getPl_spec_id());
		model.addAttribute("specList", specList);
		model.addAttribute("rc", rc);
		model.addAttribute("rcd", rcd);
		model.addAttribute("offset", offset);
		model.addAttribute("total", rcTotal);
		model.addAttribute("id", id);
		model.addAttribute("verify", verify);
		model.addAttribute("r_month", r_month);
		return "pages/base/rc_comp_show";
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@RequestMapping(value = "/rc_comp/show_verify", method = RequestMethod.GET)
	public String plPatrol_show_verify(Model model, @RequestParam Integer id) {
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(new HashMap<String,Object>());
		RunRecordcomprehensiveDetail rcd = baseService
				.queryRunRecordcomprehensiveDetailById(id);
		RunRecordcomprehensive rc = baseService
				.queryRunRecordcomprehensiveById(id);
		List<BasePipelineSection> sectionList = baseService.querySection(rc
				.getPl_id());
		List<BasePipelineSpec> specList = baseService.querySpec(rc
				.getPl_section_id());

		model.addAttribute("pl", rc.getPl_id());
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("section", rc.getPl_section_id());
		model.addAttribute("sectionList", sectionList);
		model.addAttribute("spec", rc.getPl_spec_id());
		model.addAttribute("specList", specList);
		model.addAttribute("rcd", rcd);
		model.addAttribute("rc", rc);
		model.addAttribute("verify", 1);
		return "pages/base/rc_comp_show";
	}
	
	/**
	 * 阴极保护站运行月综合记录导出
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/rc_comp/exp", method = RequestMethod.GET)
	public String plMeasure_export(Model model, HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(required = false) Integer pl,
			@RequestParam(required = false) Integer section,
			@RequestParam(required = false) Integer spec,
			@RequestParam(required = false) String r_month) {
		Map<String, Object> param = new HashMap<String, Object>();
		if (pl != null && pl.intValue() > 0) {
			param.put("pl_id", pl);
		}
		if (section != null && section.intValue() > 0) {
			param.put("pl_section_id", section);
		}
		if (spec != null && spec.intValue() > 0) {
			param.put("pl_spec_id", spec);
		}

		if (!StringUtils.isBlank(r_month)) {
			param.put("r_month", Integer.valueOf(r_month.replace("-", "")));
		}

		List<RunRecordcomprehensive> rcList = baseService
				.queryRunRecordcomprehensive(param, null);
		if (rcList.size() == 0) {
			return "redirect:/admin/base/rc_comp/list";
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
			for (RunRecordcomprehensive rc : rcList) {
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

				RunRecordcomprehensiveDetail rcd = baseService
						.queryRunRecordcomprehensiveDetailById(rc.getId());

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
	
	@RequestMapping(value = "/rc_comp/verify_save", method = RequestMethod.POST)
	public @ResponseBody String plMeasure_verify_save(Model model,
			@RequestParam Integer id, @RequestParam Integer status,
			@RequestParam String verify_desc) {
		LoginUserInfo ud = (LoginUserInfo) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		baseService.updateRunRecordcomprehensiveVerifyStatus(id, ud.getId(), status,
				verify_desc);

		return JsonUtil.toJson("OK");
	}
	
	/**
	 * 阴极保护站运行月综合记录创建
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/rc_comp/edit", method = RequestMethod.GET)
	public String rc_edit(Model model, @RequestParam Integer id,
			@RequestParam(required = false) String status) {
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(new HashMap<String,Object>());
		RunRecordcomprehensive rc = baseService.queryRunRecordcomprehensiveById(id);
		RunRecordcomprehensiveDetail rcd = baseService
				.queryRunRecordcomprehensiveDetailById(id);
		List<BasePipelineSection> sectionList = baseService.querySection(rc
				.getPl_id());
		List<BasePipelineSpec> specList = baseService.querySpec(rc
				.getPl_section_id());

		model.addAttribute("pl", rc.getPl_id());
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("section", rc.getPl_section_id());
		model.addAttribute("sectionList", sectionList);
		model.addAttribute("spec", rc.getPl_spec_id());
		model.addAttribute("specList", specList);
		model.addAttribute("rc", rc);
		model.addAttribute("rcd", rcd);
		List<BaseReceiver> br = baseService.queryLeaderList();
		model.addAttribute("br", br);
		if (StringUtils.equals(status, "1")) {
			model.addAttribute("msg", "保存成功！");
		} else if (StringUtils.equals(status, "0")) {
			model.addAttribute("msg", "保存失败！");
		}
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<Integer, Integer> m = userService.getUsersRef();
		if (m != null && m.containsKey(ud.getId())) {
			model.addAttribute("up_id", m.get(ud.getId()));
		}
		return "pages/base/rc_comp_edit";
	}
	
	@RequestMapping(value = "/rc_comp/del", method = RequestMethod.GET)
	public String pl_curve_del(Model model, @RequestParam Integer id) {
		baseService.deleteRunRecordcomprehensiveById(id);
		return "redirect:/admin/base/rc_comp/list?del=1";
	}
	
}
