package cn.hm.oil.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.hm.oil.domain.BasePipeline;
import cn.hm.oil.domain.BasePipelineSection;
import cn.hm.oil.domain.BasePipelineSpec;
import cn.hm.oil.domain.Event;
import cn.hm.oil.domain.EventAttachement;
import cn.hm.oil.domain.EventExcel;
import cn.hm.oil.domain.EventLevel;
import cn.hm.oil.domain.EventReceiver;
import cn.hm.oil.domain.EventReply;
import cn.hm.oil.domain.EventReplyAttachement;
import cn.hm.oil.domain.Prompt;
import cn.hm.oil.domain.SysUsers;
import cn.hm.oil.service.BaseService;
import cn.hm.oil.service.EventService;
import cn.hm.oil.service.UserService;
import cn.hm.oil.util.DataUtil;
import cn.hm.oil.util.DateFormatter;
import cn.hm.oil.util.FileUtils;
import cn.hm.oil.util.JsonUtil;
import cn.hm.oil.util.PageSupport;
import cn.hm.oil.util.SettingUtils;

@Controller
@RequestMapping(value = "/admin/event")
public class EventController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private BaseService baseService;
		
	@Autowired
	private UserService userService;

	/**
	 * 管理员查看一事一案列表
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/listByAdmin", method = {RequestMethod.GET, RequestMethod.POST})
	public String listByAdmin(HttpServletRequest request, Model model,@RequestParam(required=false) Integer delete,
			@RequestParam(required=false) Integer pl,
			@RequestParam(required=false) Integer section, @RequestParam(required=false) Integer spec,
			@RequestParam(required=false) String create_date,@RequestParam(required=false) String key_w) {
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
		
		if (!StringUtils.isBlank(create_date)) {
			Date date = null;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = df.parse(create_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			param.put("create_date_s", date);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			param.put("create_date_e", c.getTime());
			model.addAttribute("create_date", create_date);
		}
		if(!StringUtils.isBlank(key_w)){
			param.put("key_w", key_w);
			model.addAttribute("key_w",key_w);
		}
		PageSupport ps = PageSupport.initPageSupport(request);
		
		param.put("admin", 1);
		//param.put("status", 1);
		model.addAttribute("list_type", 1);
		List<Event> events = eventService.queryEvent(param, ps);
		model.addAttribute("events", events);
		model.addAttribute("show", 1);
		model.addAttribute("admin", 1);
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("acurl", "listByAdmin");
		if(delete!=null && delete==1){
			model.addAttribute("msg", "删除成功!");
		}
		return "pages/event/list";
	}
	
	/**
	 * 一事一案列表
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
	public String list(HttpServletRequest request, Model model,@RequestParam(required=false) Integer pl,
			@RequestParam(required=false) Integer section, @RequestParam(required=false) Integer spec,
			@RequestParam(required=false) String create_date,@RequestParam(required=false) String key_w) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
		
		if (!StringUtils.isBlank(create_date)) {
			Date date = null;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = df.parse(create_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			param.put("create_date_s", date);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			param.put("create_date_e", c.getTime());
			model.addAttribute("create_date", create_date);
		}
		if(!StringUtils.isBlank(key_w)){
			param.put("key_w", key_w);
			model.addAttribute("key_w",key_w);
		}
		
		PageSupport ps = PageSupport.initPageSupport(request);
		param.put("user_id", ud.getId());
		//审核人员可以在待处理中看到所有未关闭的一事一案
		if (ud.getEvent_checker().intValue() == 1){
			param.put("admin", 1);
			model.addAttribute("admin", 1);
		}
		//待处理中只能显示所有未关闭的一事一案
		param.put("close", 0);
		model.addAttribute("close", 0);
		
		model.addAttribute("list_type", 2);
		List<Event> events = eventService.queryEvent(param, ps);
		model.addAttribute("events", events);
		model.addAttribute("show", 1);
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("acurl", "list");
		return "pages/event/list";
	}
	
	
	/**
	 * 一事一案列表
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/list_over", method = {RequestMethod.GET, RequestMethod.POST})
	public String list_over(HttpServletRequest request, Model model,@RequestParam(required=false) Integer pl,
			@RequestParam(required=false) Integer section, @RequestParam(required=false) Integer spec,
			@RequestParam(required=false) String create_date,@RequestParam(required=false) String key_w) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
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
		
		if (!StringUtils.isBlank(create_date)) {
			Date date = null;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = df.parse(create_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			param.put("create_date_s", date);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			param.put("create_date_e", c.getTime());
			model.addAttribute("create_date", create_date);
		}
		if(!StringUtils.isBlank(key_w)){
			param.put("key_w", key_w);
			model.addAttribute("key_w",key_w);
		}
		
		PageSupport ps = PageSupport.initPageSupport(request);
		param.put("user_id", ud.getId());
		if (ud.getEvent_checker().intValue() == 1){
			param.put("admin", 1);
			model.addAttribute("admin", 1);
		}
		if (ud.getId().intValue() == 1){
			param.put("admin", 1);
			model.addAttribute("admin", 1);
		}
		//已关闭查询
		param.put("close", 1);
		model.addAttribute("close", 1);
		model.addAttribute("list_type", 3);
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(param);
		
		List<Event> events = eventService.queryEvent(param, ps);
		model.addAttribute("events", events);
		model.addAttribute("show", 1);
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("acurl", "list_over");
		return "pages/event/list";
	}
	
	
	/**
	 * 一事一案列表
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/list_show", method = {RequestMethod.GET, RequestMethod.POST})
	public String list_show(HttpServletRequest request, Model model,@RequestParam(required=false) Integer pl,
			@RequestParam(required=false) Integer section, @RequestParam(required=false) Integer spec,
			@RequestParam(required=false) String create_date,@RequestParam(required=false) String key_w) {
		
		
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
		
		if (!StringUtils.isBlank(create_date)) {
			Date date = null;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = df.parse(create_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			param.put("create_date_s", date);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			param.put("create_date_e", c.getTime());
			model.addAttribute("create_date", create_date);
		}
		if(!StringUtils.isBlank(key_w)){
			param.put("key_w", key_w);
			model.addAttribute("key_w",key_w);
		}
		
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(param);
		PageSupport ps = PageSupport.initPageSupport(request);
		//Map<String, Object> param = new HashMap<String, Object>();
		param.put("admin", 1);
		model.addAttribute("admin", 1);
		List<Event> events = eventService.queryEvent(param, ps);
		model.addAttribute("events", events);
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("acurl", "list_show");
		return "pages/event/list_show";
	}
	
	/**
	 * 创建一事一案
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String create(Model model, @RequestParam(required = false) Integer add, @RequestParam(required = false) Integer id) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if(userService.queryRoleIdByUserId(ud.getId())==3) {
			model.addAttribute("pipeLineList", baseService.queryPipeLineByUser(ud.getId()));
		} else {
			model.addAttribute("pipeLineList", baseService.queryPipeLine(new HashMap<String,Object>()));
		}
		model.addAttribute("types", eventService.queryEventType());
		
		if (id != null && id.intValue() > 0) {
			Event e = eventService.queryEventById(id);
			model.addAttribute("event", e);
			return "pages/event/detail";
		}
		
		Prompt prompt = baseService.quertPromptByType(11);
		if(prompt != null) {
			model.addAttribute("prompt", prompt);
		}
		if (add != null && add.intValue() == -1) {
			model.addAttribute("msg", "保存失败！");
		} else if (add != null && add.intValue() == 1) {
			model.addAttribute("msg", "保存成功！");
		}
		return "pages/event/create";
	}
	
	/**
	 * 一事一案修改
	 * 
	 * @param model
	 * @param add
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public String edit(Model model, @RequestParam(required = false) Integer add, @RequestParam(required = false) Integer modify, @RequestParam(required = false) Integer id, @RequestParam(required = false) Integer admin, @RequestParam(required = false) Integer tips_id) {
		if (tips_id != null && tips_id.intValue() > 0)
			eventService.deleteTips(tips_id);
		Event e = eventService.queryEventById(id);
		if (e != null) {
			model.addAttribute("pipeLineList",baseService.queryPipeLineByUser(e.getCreated_by()).size() == 0 ? baseService.queryPipeLine(new HashMap<String,Object>()) : baseService.queryPipeLineByUser(e.getCreated_by()));
			model.addAttribute("sectionList",baseService.querySection(e.getPl_id()));
			model.addAttribute("specList",baseService.querySpec(e.getPl_section_id()));
			model.addAttribute("types", eventService.queryEventType());
			model.addAttribute("e", e);
			List<EventAttachement> ealist = eventService
					.queryEventAttachementByEventId(e.getId());
			model.addAttribute("ealist", ealist);

			if (add != null && add.intValue() == -1) {
				model.addAttribute("msg", "保存失败！");
			} else if (add != null && add.intValue() == 1) {
				model.addAttribute("msg", "保存成功！");
			}
			if (admin != null) {
				if(modify!=null && modify==1)model.addAttribute("msg", "修改成功");
				model.addAttribute("admin", admin);
				List<EventReply> replies = eventService.queryEventReply(id);
				List<EventReply> leaderPHReplies = new ArrayList<EventReply>();
				for (EventReply er : replies) {
					if (er.getLeader() != null && er.getLeader().intValue() == 1) {
						leaderPHReplies.add(er);
					}
				}
				replies.removeAll(leaderPHReplies);
				model.addAttribute("leaderPHReplies", leaderPHReplies);
				model.addAttribute("replies", replies);
			}
		} else {
			model.addAttribute("msg", "该一事一案不存在!");
			return "pages/event/blank";
		}

		return "pages/event/edit";
	}
	
	/**
	 * 删除一事一案
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/del",method = RequestMethod.GET)
	public String del(Model model,@RequestParam Integer id){
		eventService.deleteEvent(id);
		return "redirect:/admin/event/listByAdmin?delete=1";
	}
	
	/**
	 * 删除一事一案回复信息
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/del_reply",method = RequestMethod.GET)
	public @ResponseBody String del_reply(Model model,@RequestParam Integer id){
		eventService.deleteEventReplyById(id);
		return "ok";
	}
	
	/**
	 * 删除一事一案回复附件
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/del_attachement",method = RequestMethod.GET)
	public @ResponseBody String del_attachement(Model model,@RequestParam Integer id){
		eventService.deleteEventReplyAttachementById(id);
		return "ok";
	}
	
	/**
	 * 打开修改回复弹出框
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/edit_reply/{id}",method = RequestMethod.GET)
	public String edit_reply(Model model,@PathVariable Integer id){
		EventReply er = eventService.queryEventReplyById(id);
		model.addAttribute("er", er);
		return "pages/event/edit_reply";
	}
	
	/**
	 * 保丰修改回复的内容
	 * 
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/save_reply",method = RequestMethod.POST)
	public  String save_reply(Model model,@RequestParam Integer id, @RequestParam String content, @RequestParam String create_time, @RequestParam(required = false) String filename) {
		List<String> fileNames = new ArrayList<String>();
		if (!StringUtils.isBlank(filename)) {
			String[] names = filename.split(";");
			for (String name : names) {
				if (!StringUtils.isBlank(name)) {
					try {
						String fn = DataUtil.moveToFileDir(name);
						fileNames.add(fn);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		eventService.updateEventReply(id, content,fileNames,DateFormatter.stringToDate(create_time));
		EventReply er = eventService.queryEventReplyById(id);
		return "redirect:/admin/event/edit?admin=1&&modify=1&&id="+er.getEvent_id();
	}
	
	/**
	 * 保存一事一案
	 * 
	 * @param model
	 * @param add
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(HttpServletRequest request, Model model, @RequestParam Integer pl, @RequestParam Integer section, @RequestParam Integer spec,
			@RequestParam String discover_date, @RequestParam String position_start, @RequestParam(required=false) String position_end,
			@RequestParam String longitude, @RequestParam String latitude, @RequestParam Integer type_id,
			@RequestParam String content, @RequestParam String remark, @RequestParam(required=false) String report_object,
			@RequestParam Integer send_notice, @RequestParam Integer is_warn, @RequestParam(required = false) Integer id,
			@RequestParam(required = false) Integer admin,
			@RequestParam(required=false) String edit, @RequestParam(required=false) String keyword,
			@RequestParam(required=false) String filename,
			@RequestParam(required=false) String ef_length,
			@RequestParam(required=false) String pl_no,
			@RequestParam(required=false) String risk,
			@RequestParam(required=false) String link_name,
			@RequestParam(required=false) String link_duty,
			@RequestParam(required=false) String link_method,
			@RequestParam(required=false) String unit_name,
			@RequestParam(required=false) String unit_address,
			@RequestParam(required=false) String unit_post,
			@RequestParam Integer is_ma_remark) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		Event event = new Event();
		if (!StringUtils.isBlank(filename)) {
			List<String> fileNames = new ArrayList<String>();

			String[] names = filename.split(";");
			for (String name : names) {
				if (!StringUtils.isBlank(name)) {
					try {
						String fn = DataUtil.moveToFileDir(name);
						fileNames.add(fn);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			event.setFileNames(fileNames);
		}
		if(send_notice.intValue() == 1) {
			try {
				List<String> paths = DataUtil.uploadFile(request, "notice_file");
				if (paths!=null && !StringUtils.isBlank(paths.get(0))) {
					event.setNotice_file(paths.get(0));
					event.setSend_notice(true);
				} else {
					if(id!=null && id.intValue()>0){
						Event evt = eventService.queryEventById(id);
						if(!StringUtils.isBlank(evt.getNotice_file())){
							event.setSend_notice(true);
						} else {
							event.setSend_notice(false);
						}
					} else {
						event.setSend_notice(false);
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event.setSend_notice(false);
		}
		
		if(is_warn.intValue() == 1) {
			try {
				List<String> paths = DataUtil.uploadFile(request, "warn_file");
				if (paths!=null &&!StringUtils.isBlank(paths.get(0))) {
					event.setWarn_file(paths.get(0));
					event.setIs_warn(true);
				} else {
					if(id!=null && id.intValue()>0){
						Event evt = eventService.queryEventById(id);
						if(!StringUtils.isBlank(evt.getWarn_file())){
							event.setIs_warn(true);
						} else {
							event.setIs_warn(false);
						}
					} else {
						event.setIs_warn(false);
					}
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event.setIs_warn(false);
		}
		
		if(is_ma_remark.intValue() == 1) {
			try {
				List<String> paths = DataUtil.uploadFile(request, "ma_remark");
				if (paths!=null &&!StringUtils.isBlank(paths.get(0))) {
					event.setMa_remark(paths.get(0));
					event.setIs_ma_remark(true);
				} else {
					if(id!=null && id.intValue()>0){
						Event evt = eventService.queryEventById(id);
						if(!StringUtils.isBlank(evt.getMa_remark())){
							event.setIs_ma_remark(true);
						} else {
							event.setIs_ma_remark(false);
						}
					} else {
						event.setIs_ma_remark(false);
					}
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event.setIs_ma_remark(false);
		}
		
		try {
			List<String> paths = DataUtil.uploadFile(request, "moon_file");
			if (!CollectionUtils.isEmpty(paths) &&!StringUtils.isBlank(paths.get(0))) {
				event.setMoon_file(paths.get(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			List<String> paths = DataUtil.uploadFile(request, "scene_file");
			if (!CollectionUtils.isEmpty(paths)  &&!StringUtils.isBlank(paths.get(0))) {
				event.setScene_file(paths.get(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		event.setContent(content);
		event.setCreated_by(ud.getId());
		event.setDiscover_date(DateFormatter.stringToDate(discover_date));
		event.setLongitude(longitude);
		event.setLatitude(latitude);
		event.setId(id);
		//event.setIs_warn(is_warn.intValue() == 1);
		event.setPl_id(pl);
		event.setPl_section_id(section);
		event.setPl_spec_id(spec);
		event.setPosition_end(position_end);
		event.setPosition_start(position_start);
		event.setRemark(remark);
		event.setReport_object(report_object);
		event.setType_id(type_id);
		event.setEf_length(ef_length);
		event.setPl_no(pl_no);
		event.setRisk(risk);
		event.setLink_name(link_name);
		/*event.setLink_duty(link_duty);
		event.setLink_method(link_method);*/
		event.setUnit_name(unit_name);
		/*event.setUnit_address(unit_address);
		event.setUnit_post(unit_post);*/
		if(admin!=null){
			event.setStatus(1);
		} else{
			event.setStatus(0);
		}
		event.setKeyword(keyword);
		//event.setSend_notice(send_notice.intValue() == 1);
		eventService.saveEvent(event);
		if (edit != null)
			return "redirect:/admin/event/edit?add=1&id=" + id;
		return "redirect:/admin/event/create?add=1";
	}
	
	
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public String save(HttpServletRequest request, Model model,@RequestParam(required = false) Integer e_id,
			@RequestParam(required = false) Integer aType) {
		EventExcel ee = new EventExcel();
		ee.setaType(aType);
		ee.setE_id(e_id);
		try {
			List<String> paths = DataUtil.uploadFile(request, "file");
			if (!CollectionUtils.isEmpty(paths)  &&!StringUtils.isBlank(paths.get(0))) {
				ee.setPath(paths.get(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		eventService.saveEventExcel(ee);
		return "redirect:/admin/event/detail?id="+e_id;
	}
	
	/**
	 * 一事一案查看
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public String detail(Model model, @RequestParam Integer id, @RequestParam(required=false) Integer success,
			@RequestParam(required=false) Integer tips_id) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (tips_id != null && tips_id.intValue() > 0)
			eventService.deleteTips(tips_id);
		Event e = eventService.queryEventById(id);
		if(e!=null){
		
		List<EventExcel> ees = eventService.queryEventExcelByEventId(id);
		model.addAttribute("ees", ees);
		
		model.addAttribute("pipeLineList", baseService.queryPipeLine(new HashMap<String,Object>()));
		model.addAttribute("sectionList", baseService.querySection(e.getPl_id()));
		model.addAttribute("specList", baseService.querySpec(e.getPl_section_id()));
		model.addAttribute("types", eventService.queryEventType());
		model.addAttribute("e", e);
		
		List<BasePipeline> basePipeLineList = baseService.queryPipeLine(new HashMap<String,Object>());
		model.addAttribute("basePipeLineList", basePipeLineList);
		
		if (ud.getLevel().intValue() == 4) {//普通干部， 4级
			List<EventReceiver> allReceivers = eventService.queryWorkerList(null, null);
			model.addAttribute("allReceivers", allReceivers);
			
			
		} else {
			List<EventReceiver> leaders = eventService.queryLeaderList(ud.getLevel() + 1);
			model.addAttribute("leaders", leaders);
		}
		
		List<EventReceiver> receivers = eventService.queryEventReceiver(id);
		model.addAttribute("receivers", receivers);
		
		List<EventReply> replies = eventService.queryEventReply(id);
		List<EventReply> leaderPHReplies = new ArrayList<EventReply>();
		for (EventReply er : replies) {
			if (er.getLeader() != null && er.getLeader().intValue() == 1) {
				leaderPHReplies.add(er);
			}
		}
		replies.removeAll(leaderPHReplies);
		model.addAttribute("leaderPHReplies", leaderPHReplies);
		model.addAttribute("replies", replies);
		if (success != null) {
			model.addAttribute("msg", "操作成功！");
		}
		List<EventLevel> els = eventService.queryEventLevel();
		
		model.addAttribute("els", els);
		List<EventAttachement> ealist = eventService.queryEventAttachementByEventId(e.getId());
		model.addAttribute("ealist", ealist);
		} else {
			model.addAttribute("msg", "一事一案不存在");
			return "pages/event/blank";
		}
		return ud.getId().intValue() == 1 ? "pages/event/detailByAdmin" : "pages/event/detail";
	}
	
	/**
	 * 领导更改级别
	 */
	@RequestMapping(value="/update_jibie" ,method = RequestMethod.GET)
	public void update_jibie(PrintWriter out,@RequestParam Integer id,@RequestParam Integer event_level){
		eventService.updateEventLevel(id, event_level);
		out.write(JsonUtil.toJson("ok"));
		out.flush();
	}
	
	
	/**
	 * 一事一案查看
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/detail_show", method = RequestMethod.GET)
	public String detail_show(Model model, @RequestParam Integer id, @RequestParam(required=false) Integer success, @RequestParam(required=false) Integer tips_id) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (tips_id != null && tips_id.intValue() > 0)
			eventService.deleteTips(tips_id);
		Event e = eventService.queryEventById(id);
		if(e!=null){
		model.addAttribute("pipeLineList", baseService.queryPipeLine(new HashMap<String,Object>()));
		model.addAttribute("sectionList", baseService.querySection(e.getPl_id()));
		model.addAttribute("specList", baseService.querySpec(e.getPl_section_id()));
		model.addAttribute("types", eventService.queryEventType());
		model.addAttribute("e", e);
		
		List<BasePipeline> basePipeLineList = baseService.queryPipeLine(new HashMap<String,Object>());
		model.addAttribute("basePipeLineList", basePipeLineList);
		
		if (ud.getLevel().intValue() == 4) {//普通干部， 4级
			List<EventReceiver> allReceivers = eventService.queryWorkerList(null, null);
			model.addAttribute("allReceivers", allReceivers);
			
			
		} else {
			List<EventReceiver> leaders = eventService.queryLeaderList(ud.getLevel() + 1);
			model.addAttribute("leaders", leaders);
		}
		List<EventReceiver> receivers = eventService.queryEventReceiver(id);
		model.addAttribute("receivers", receivers);
		
		List<EventReply> replies = eventService.queryEventReply(id);
		List<EventReply> leaderPHReplies = new ArrayList<EventReply>();
		for (EventReply er : replies) {
			if (er.getLeader() != null && er.getLeader().intValue() == 1) {
				leaderPHReplies.add(er);
			}
		}
		replies.removeAll(leaderPHReplies);
		model.addAttribute("leaderPHReplies", leaderPHReplies);
		model.addAttribute("replies", replies);
		if (success != null) {
			model.addAttribute("msg", "操作成功！");
		}
		List<EventLevel> els = eventService.queryEventLevel();
		model.addAttribute("els", els);
		List<EventAttachement> ealist = eventService.queryEventAttachementByEventId(e.getId());
		model.addAttribute("ealist", ealist);
		} else {
			model.addAttribute("msg", "一事一案不存在");
			return "pages/event/blank";
		}
		return "pages/event/detail_show";
	}
	
	@RequestMapping(value = "/findWorker", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody List<EventReceiver> findWorker(HttpServletRequest request, Model model, @RequestParam(required=false) Integer pl_id, 
			@RequestParam(required=false) Integer section_id) {
		if (pl_id != null && pl_id.intValue() <= 0)
			pl_id = null;
		if (section_id != null && section_id.intValue() <= 0)
			section_id = null;
		List<EventReceiver> receivers = eventService.queryWorkerList(pl_id, section_id);
		return receivers;
	}
	
	@RequestMapping(value = "/verify", method = {RequestMethod.GET, RequestMethod.POST})
	public String verify(HttpServletRequest request, Model model,@RequestParam(required=false) Integer pl,
			@RequestParam(required=false) Integer section, @RequestParam(required=false) Integer spec,
			@RequestParam(required=false) String create_date,@RequestParam(required=false) String key_w) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
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
		
		if (!StringUtils.isBlank(create_date)) {
			Date date = null;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = df.parse(create_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			param.put("create_date_s", date);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			param.put("create_date_e", c.getTime());
			model.addAttribute("create_date", create_date);
		}
		if(!StringUtils.isBlank(key_w)){
			param.put("key_w", key_w);
			model.addAttribute("key_w",key_w);
		}
		PageSupport ps = PageSupport.initPageSupport(request);
		//Map<String, Object> param = new HashMap<String, Object>();
		param.put("user_id", ud.getId());
		if(ud.getLevel()!=1){
			param.put("admin", 1);
		}
		param.put("verify", 1);
		//param.put("status", 0);
		List<BasePipeline> pipeLineList = baseService.queryPipeLine(param);
		
		List<Event> events = eventService.queryEvent(param, ps);
		model.addAttribute("events", events);
		model.addAttribute("verify", 1);
		model.addAttribute("pipeLineList", pipeLineList);
		model.addAttribute("acurl", "verify");
		return "pages/event/list";
	}
	
	@RequestMapping(value = "/verify_detail", method = {RequestMethod.GET, RequestMethod.POST})
	public String verifyDetail(Model model, @RequestParam Integer id, @RequestParam(required=false) Integer success,@RequestParam(required=false) Integer tips_id) {
		if (tips_id != null && tips_id.intValue() > 0)
			eventService.deleteTips(tips_id);
		Event e = eventService.queryEventById(id);
		if(e!=null){
		List<BasePipeline> basePipeLineList = baseService.queryPipeLine(new HashMap<String,Object>());
		model.addAttribute("basePipeLineList", basePipeLineList);
		
		List<EventReceiver> allReceivers = eventService.queryWorkerList(null, null);
		model.addAttribute("allReceivers", allReceivers);
		
		List<EventReceiver> receivers = eventService.queryEventReceiver(id);
		model.addAttribute("receivers", receivers);
		
		List<EventReply> replies = eventService.queryEventReply(id);
		model.addAttribute("replies", replies);
		
		List<EventLevel> els = eventService.queryEventLevel();
		
		model.addAttribute("els", els);
		
		model.addAttribute("pipeLineList", basePipeLineList);
		model.addAttribute("sectionList", baseService.querySection(e.getPl_id()));
		model.addAttribute("specList", baseService.querySpec(e.getPl_section_id()));
		model.addAttribute("types", eventService.queryEventType());
		model.addAttribute("e", e);
		if (success != null) {
			model.addAttribute("msg", "操作成功！");
		}
		List<EventAttachement> ealist = eventService.queryEventAttachementByEventId(e.getId());
		model.addAttribute("ealist", ealist);
		} else {
			model.addAttribute("msg", "一事一案不存在");
			return "pages/event/blank";
		}
		return "pages/event/verify";
	}
	
	@RequestMapping(value = "/verify_save", method = RequestMethod.POST)
	public String verifySave(HttpServletRequest request,Model model, @RequestParam Integer id, @RequestParam Integer status, @RequestParam Integer verify_level,
			@RequestParam(required=false) Integer type_id, String message, @RequestParam String users, 
			@RequestParam(required=false) Integer up_to_level) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(ud==null){
			return "pages/frame/login";
		}
		if (type_id != null && type_id.intValue() != 0 && type_id.intValue() != 1 && type_id.intValue() != 2) {
			type_id = null;
		}
		
		List<Integer> receivers = new ArrayList<Integer>();
		if (!StringUtils.isBlank(users)) {
			String [] userArr = users.split(",");
			for (String ids : userArr) {
				receivers.add(Integer.valueOf(ids));
			}
		}
		/*String moon_file="";
		try {
			List<String> paths = DataUtil.uploadFile(request, "moon_file");
			if (!StringUtils.isBlank(paths.get(0))) {
				moon_file=paths.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		eventService.updateEventStatus(status, message, id, receivers, type_id, up_to_level, null,ud.getName(),verify_level);
		return "redirect:/admin/event/verify_detail?success=1&id=" + id;
	}
	
	/**
	 * 查看详情页面重新保存指派人员
	 */
	@RequestMapping(value = "/saveWorker", method = {RequestMethod.POST,RequestMethod.GET})
	public String saveWorker(Model model, @RequestParam Integer id, @RequestParam(required=false) String users, 
			@RequestParam(required=false) Integer up_to_level, @RequestParam(required=false) String message,
			@RequestParam(required=false) Integer close) {
		if (close != null && close.intValue() == 1) {
			eventService.updateEventClose(id, message);
			return "redirect:/admin/event/detail_show?success=1&id=" + id;
		} else {
			List<Integer> receivers = new ArrayList<Integer>();
			if (!StringUtils.isBlank(users)) {
				String [] userArr = users.split(",");
				for (String ids : userArr) {
					if (!StringUtils.isBlank(ids))
						receivers.add(Integer.valueOf(ids));
				}
			}
			eventService.updateEventStatus(null, null, id, receivers, null, up_to_level, message,null,0);
			return "redirect:/admin/event/detail?success=1&id=" + id;
		}
	}
	
	@RequestMapping(value = "/reply", method = RequestMethod.POST)
	public String reply(Model model, HttpServletRequest request,  @RequestParam String msg_content, @RequestParam Integer id,
			@RequestParam(required=false) String filename) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		EventReply nr = new EventReply();
		nr.setContent(msg_content);
		nr.setEvent_id(id);
		nr.setUser_id(ud.getId());
		try {
			if (!StringUtils.isBlank(filename)) {
				List<String> fileNames = new ArrayList<String>();

				String[] names = filename.split(";");
				for (String name : names) {
					if (!StringUtils.isBlank(name)) {
						try {
							String fn = DataUtil.moveToFileDir(name);
							fileNames.add(fn);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				nr.setFileNames(fileNames);
			}
			/*List<String> paths = DataUtil.uploadFile(request, "file");
			if (!CollectionUtils.isEmpty(paths)) {
				nr.setPath(paths.get(0));
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		eventService.insertEventReply(nr);
		return "redirect:/admin/event/detail?success=1&id=" + id;
	}
	
	@RequestMapping(value = "/del_ea", method = RequestMethod.GET)
	public @ResponseBody String del_ea(Model model, HttpServletRequest request,@RequestParam Integer id){
		eventService.deleteEventAttachementById(id);
		return "ok";
	}
	
	/**
	 * 一事一案导出
	 * @param model
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/exp")
	public String event_exp(Model model, HttpServletRequest request, HttpServletResponse response, @RequestParam(required=false) Integer pl,
			@RequestParam(required=false) Integer section, @RequestParam(required=false) Integer spec,
			@RequestParam(required=false) String create_date,@RequestParam(required=false) String key_w
			, @RequestParam(required=false) String list_type, @RequestParam(required=false) String checked_id
			, @RequestParam(required=false) String admin, @RequestParam(required=false) String close) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(ud==null){
			return "redirect:/login";
		}
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
		
		if(checked_id != null && !StringUtils.isBlank(checked_id)) {
			String[] ids = checked_id.split(",");
			param.put("checked_id", ids);
		}
		
		
		
		if (!StringUtils.isBlank(create_date)) {
			Date date = null;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date = df.parse(create_date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			param.put("create_date_s", date);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			param.put("create_date_e", c.getTime());
			//model.addAttribute("create_date", create_date);
		}
		
		if(!StringUtils.isBlank(key_w)){
			param.put("key_w", key_w);
		}
		PageSupport ps = PageSupport.initPageSupport(request);
		//Map<String, Object> param = new HashMap<String, Object>();
		if(!StringUtils.isBlank(close)){
			param.put("close", close);
		}
		if(!StringUtils.isBlank(admin)){
			param.put("admin", admin);
		}
		//System.out.println(admin+"--------");
		param.put("user_id", ud.getId());
		List<Event> events = eventService.queryEvent(param, ps);
		
		if(events.size()==0) {
			return null;
		}
		
		String sep = System.getProperty("file.separator");
		String fileDir = SettingUtils.getCommonSetting("upload.file.temp.path");// 存放文件文件夹名称
		
		Date dt = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		String ct = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH)+1) + "-" + cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" +cal.get(Calendar.MINUTE) + "-" +cal.get(Calendar.SECOND) + "-" +cal.get(Calendar.MILLISECOND);
		
		String path=fileDir + sep + "excel_"+ct;
		File dirPath = new File(path);
		if (!dirPath.exists()) {
			dirPath.mkdirs();
		}
		
		for(Event eventx : events) {
		
		try {
			Event event = eventService.queryEventById(eventx.getId());
			String exceldpath=path + sep + event.getPl_name() + event.getPl_section_name() + event.getPl_spec_name().replace(".", "_") + "_" + DateFormatter.dateToString(event.getCreate_time(),"yyyy-MM-dd_hh-mm-ss");
			File exceldirPath = new File(exceldpath);
			if (!exceldirPath.exists()) {
				exceldirPath.mkdirs();
			}
			String excelPath = exceldpath + sep + event.getPl_name() + event.getPl_section_name() + event.getPl_spec_name().replace(".", "_") + "_" + DateFormatter.dateToString(event.getCreate_time(),"yyyy-MM-dd_hh-mm-ss") + ".xls";
			FileOutputStream fileOut = new FileOutputStream(excelPath);
			/*String excelName = new String(en.getBytes("UTF-8"), "ISO-8859-1");
			response.setContentType("APPLICATION/OCTET-STREAM;charset=UTF-8");
			if (request.getHeader("user-agent").toLowerCase().indexOf("msie") > 0)// IE浏览器
			{
				response.setHeader("content-disposition", "filename="
						+ java.net.URLEncoder.encode(en, "UTF-8"));
			} else {
				response.setHeader("Content-Disposition",
						"attachment; filename=" + excelName);
			}*/
			
			List<EventReply> replys = eventService.queryEventReply(event.getId());
			
			// 实例化输出流
			
			HSSFWorkbook work = new HSSFWorkbook();
			HSSFSheet sheet1 = work.createSheet("sheet1");
			HSSFCell cell;
			
			//合并单元格
			sheet1.addMergedRegion(new CellRangeAddress(0,0,0,5));
			sheet1.addMergedRegion(new CellRangeAddress(1,1,1,3));
			sheet1.addMergedRegion(new CellRangeAddress(2,2,1,3));
			sheet1.addMergedRegion(new CellRangeAddress(4,4,1,3));
			sheet1.addMergedRegion(new CellRangeAddress(6,6,1,3));
			sheet1.addMergedRegion(new CellRangeAddress(7,7,1,3));
			sheet1.addMergedRegion(new CellRangeAddress(8,8,0,5));
			sheet1.addMergedRegion(new CellRangeAddress(9,9,1,5));
			sheet1.addMergedRegion(new CellRangeAddress(10,10,1,5));
			sheet1.addMergedRegion(new CellRangeAddress(11,11,0,1));
			sheet1.addMergedRegion(new CellRangeAddress(11,11,3,4));
			sheet1.addMergedRegion(new CellRangeAddress(12,12,1,5));
			sheet1.addMergedRegion(new CellRangeAddress(13,13,1,5));
			sheet1.addMergedRegion(new CellRangeAddress(14,14,1,5));
			sheet1.addMergedRegion(new CellRangeAddress(15,15,1,5));
			sheet1.addMergedRegion(new CellRangeAddress(16,16,1,3));

			// 标题格式
			CellStyle titleStyle = work.createCellStyle();
			// 表头格式
			CellStyle dataStyle = work.createCellStyle();
			// 内容格式
			CellStyle valueStyle = work.createCellStyle();
			
			HSSFCellStyle rowStyle = work.createCellStyle();

			rowStyle.setBorderBottom(CellStyle.BORDER_THIN);
			rowStyle.setBorderLeft(CellStyle.BORDER_THIN);
			rowStyle.setBorderRight(CellStyle.BORDER_THIN);
			rowStyle.setBorderTop(CellStyle.BORDER_THIN);
			
			titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
			titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			titleStyle.setWrapText(true);

			dataStyle.setAlignment(CellStyle.ALIGN_RIGHT);//水平方向对齐
			dataStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);// 垂直方向的对齐方式
			dataStyle.setWrapText(true);//自动换行
			dataStyle.setBorderBottom(CellStyle.BORDER_THIN);
			dataStyle.setBorderLeft(CellStyle.BORDER_THIN);
			dataStyle.setBorderRight(CellStyle.BORDER_THIN);
			dataStyle.setBorderTop(CellStyle.BORDER_THIN);
			
			valueStyle.setAlignment(CellStyle.ALIGN_LEFT);//水平方向对齐
			valueStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);// 垂直方向的对齐方式
			valueStyle.setWrapText(true);//自动换行
			valueStyle.setBorderBottom(CellStyle.BORDER_THIN);
			valueStyle.setBorderLeft(CellStyle.BORDER_THIN);
			valueStyle.setBorderRight(CellStyle.BORDER_THIN);
			valueStyle.setBorderTop(CellStyle.BORDER_THIN);

			// 表头字体
			Font titlefont = work.createFont();
			// 内容字体
			Font datafont = work.createFont();

			titlefont.setFontHeightInPoints((short) 18);
			titlefont.setFontName("黑体");
			// titlefont.setBoldweight(Font.BOLDWEIGHT_BOLD);

			datafont.setFontHeightInPoints((short) 11);
			datafont.setFontName("楷体");
			// datafont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			// datafont.setColor(HSSFColor.RED.index);

			// 把字体加入到格式中
			// cellStyle.setFont(font);
			titleStyle.setFont(titlefont);
			dataStyle.setFont(datafont);
			valueStyle.setFont(datafont);

			// 设置列宽度
			sheet1.setColumnWidth(0, 18 * 256);
			sheet1.setColumnWidth(1, 13 * 256);
			sheet1.setColumnWidth(2, 12 * 256);
			sheet1.setColumnWidth(3, 11 * 256);
			sheet1.setColumnWidth(4, 18 * 256);
			sheet1.setColumnWidth(5, 16 * 256);

			// 新建行
			HSSFRow row = sheet1.createRow(0);
			// 新建单元格
			row.setHeightInPoints((float) 30.25);
			cell = row.createCell(0);
			// 设置内容
			cell.setCellValue("一事一案");
			// 设置单元格格式
			cell.setCellStyle(titleStyle);
			// 设置单元格内容类型
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);

			row = sheet1.createRow(1);
			row.setHeightInPoints((float) 30.00);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("管线段落规格：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getPl_name()+";"+event.getPl_section_name() + ";" + event.getPl_spec_name());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(5);
			cell.setCellValue(DateFormatter.dateToString(event.getDiscover_date(), "yyyy/MM/dd hh:mm:ss"));
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);

			row = sheet1.createRow(2);
			row.setHeightInPoints((float) 30.00);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("地段（点）具体到村、社、组：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getPosition_start());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			if(event.getVerify_level() > 0) {
				cell = row.createCell(5);
				cell.setCellValue(event.getTypeName() + "；" + event.getCode_no());
				cell.setCellStyle(valueStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			}
			
			row = sheet1.createRow(3);
			row.setHeightInPoints((float) 19.50);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("经度：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getLongitude());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(2);
			cell.setCellValue("纬度：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(3);
			cell.setCellValue(event.getLatitude());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(4);
			cell.setCellValue("汇报人：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(5);
			cell.setCellValue(event.getReporter());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(4);
			row.setHeightInPoints((float) 19.50);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("关键字：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getKeyword());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(4);
			cell.setCellValue("审核人：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			//审核人未弄完
			cell = row.createCell(5);
			cell.setCellValue(event.getReport_object());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(5);
			row.setHeightInPoints((float) 19.50);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("问题类型：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getTypeName());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(4);
			cell.setCellValue("审核状态：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			String status = "";
			switch (event.getStatus()) {
			case 1:
				status = "审核通过";
				break;
			case 2:
				status = "审核不通过";
				break;

			default:
				status = "待审核";
				break;
			}
			
			cell = row.createCell(5);
			cell.setCellValue(status);
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			
			row = sheet1.createRow(6);
			row.setHeightInPoints((float) 19.50);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("编号：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getCode_no()==null?"":event.getCode_no());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(4);
			cell.setCellValue("等级：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			String level = "";
			switch (event.getVerify_level()) {
			case 1:
				level = "管护班级";
				break;
			case 2:
				level = "作业区级";
				break;
			case 3:
				level = "输气处级";
				break;
			default:
				level = "";
				break;
			}
			
			cell = row.createCell(5);
			cell.setCellValue(level);
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			
			row = sheet1.createRow(7);
			row.setHeightInPoints((float) 19.50);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("影响管线长度：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getEf_length()==null?"":event.getEf_length());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(4);
			cell.setCellValue("管线桩号：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			
			cell = row.createCell(5);
			cell.setCellValue(event.getPl_no()==null?"":event.getPl_no());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(8);
			row.setHeightInPoints((float) 12.00);
			row.setRowStyle(rowStyle);
			
			row = sheet1.createRow(9);
			row.setHeightInPoints((float) 144.75);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("内容：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getContent());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(10);
			row.setHeightInPoints((float) 60.00);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("备注：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getRemark());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(11);
			row.setHeightInPoints((float) 21.00);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("是否发放安全隐患告知书：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(2);
			cell.setCellValue("是");
//			cell.setCellValue(event.getSend_notice()==true?"是":"否");
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			String safepath=exceldpath + sep + "安全隐患告知书";
			File safeDirPath = new File(safepath);
			if (!safeDirPath.exists()) {
				safeDirPath.mkdirs();
			}
			if(event.getSend_notice()==true){
				FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + event.getNotice_file(), safepath+sep+"安全隐患告知书"+event.getNotice_file().substring(event.getNotice_file().lastIndexOf(".")));
			}
			
			cell = row.createCell(3);
			cell.setCellValue("是否现场加密警示标志：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(5);
			cell.setCellValue("是");
			//cell.setCellValue(event.getIs_warn()==true?"是":"否");
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			String biaopath=exceldpath + sep + "现场加密警示标志";
			File biaoDirPath = new File(biaopath);
			if (!biaoDirPath.exists()) {
				biaoDirPath.mkdirs();
			}
			if(event.getIs_warn()==true){
				FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + event.getWarn_file(), biaopath+sep+"现场加密警示标志"+event.getWarn_file().substring(event.getWarn_file().lastIndexOf(".")));
			}
			
			String xianpath=exceldpath + sep + "现场示意图";
			File xianDirPath = new File(xianpath);
			if (!xianDirPath.exists()) {
				xianDirPath.mkdirs();
			}
			if(!StringUtils.isBlank(event.getScene_file())){
				FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + event.getScene_file(), xianpath+sep+"现场示意图"+event.getScene_file().substring(event.getScene_file().lastIndexOf(".")));
			}
			
			String weipath=exceldpath + sep + "卫星示意图";
			File weiDirPath = new File(weipath);
			if (!weiDirPath.exists()) {
				weiDirPath.mkdirs();
			}
			if(!StringUtils.isBlank(event.getMoon_file())){
				FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + event.getMoon_file(), weipath+sep+"卫星示意图"+event.getMoon_file().substring(event.getMoon_file().lastIndexOf(".")));
			}
			
			String tanpath=exceldpath + sep + "探管记录";
			File tanDirPath = new File(tanpath);
			if (!tanDirPath.exists()) {
				tanDirPath.mkdirs();
			}
			if(!StringUtils.isBlank(event.getMa_remark())){
				FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + event.getMa_remark(), tanpath+sep+"探管记录"+event.getMa_remark().substring(event.getMa_remark().lastIndexOf(".")));
			}
			
			List<EventExcel> ees = eventService.queryEventExcelByEventId(event.getId());
			String eventexcelpath=exceldpath + sep + "管道风险个体事件附件表格";
			File eventexcelpathDir = new File(eventexcelpath);
			if (!eventexcelpathDir.exists()) {
				eventexcelpathDir.mkdirs();
			}
			for(EventExcel e:ees){
				switch(e.getaType()){
				case 1:{
					String eventexcelpathSub=eventexcelpath + sep + "管道信息记录表";
					File eventexcelpathSubDir = new File(eventexcelpathSub);
					if (!eventexcelpathSubDir.exists()) {
						eventexcelpathSubDir.mkdirs();
					}
					if(!StringUtils.isBlank(e.getPath())){
						FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + e.getPath(), eventexcelpathSub+sep+e.getPath().replace("\\", "/").substring(e.getPath().replace("\\", "/").lastIndexOf("/")));
					}
					break;
				}
				case 2:{
					String eventexcelpathSub=eventexcelpath + sep + "基础信息表";
					File eventexcelpathSubDir = new File(eventexcelpathSub);
					if (!eventexcelpathSubDir.exists()) {
						eventexcelpathSubDir.mkdirs();
					}
					if(!StringUtils.isBlank(e.getPath())){
						FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + e.getPath(), eventexcelpathSub+sep+e.getPath().replace("\\", "/").substring(e.getPath().replace("\\", "/").lastIndexOf("/")));
					}
					break;
				}
				case 3:{
					String eventexcelpathSub=eventexcelpath + sep + "会商意见处理表";
					File eventexcelpathSubDir = new File(eventexcelpathSub);
					if (!eventexcelpathSubDir.exists()) {
						eventexcelpathSubDir.mkdirs();
					}
					if(!StringUtils.isBlank(e.getPath())){
						FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + e.getPath(), eventexcelpathSub+sep+e.getPath().replace("\\", "/").substring(e.getPath().replace("\\", "/").lastIndexOf("/")));
					}
					break;
				}
				case 4:{
					String eventexcelpathSub=eventexcelpath + sep + "现场处置记录";
					File eventexcelpathSubDir = new File(eventexcelpathSub);
					if (!eventexcelpathSubDir.exists()) {
						eventexcelpathSubDir.mkdirs();
					}
					if(!StringUtils.isBlank(e.getPath())){
						FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + e.getPath(), eventexcelpathSub+sep+e.getPath().replace("\\", "/").substring(e.getPath().replace("\\", "/").lastIndexOf("/")));
					}
					break;
				}
				case 5:{
					String eventexcelpathSub=eventexcelpath + sep + "现场验收记录";
					File eventexcelpathSubDir = new File(eventexcelpathSub);
					if (!eventexcelpathSubDir.exists()) {
						eventexcelpathSubDir.mkdirs();
					}
					if(!StringUtils.isBlank(e.getPath())){
						FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + e.getPath(), eventexcelpathSub+sep+e.getPath().replace("\\", "/").substring(e.getPath().replace("\\", "/").lastIndexOf("/")));
					}
					break;
				}
				case 6:{
					String eventexcelpathSub=eventexcelpath + sep + "销佶确认表";
					File eventexcelpathSubDir = new File(eventexcelpathSub);
					if (!eventexcelpathSubDir.exists()) {
						eventexcelpathSubDir.mkdirs();
					}
					if(!StringUtils.isBlank(e.getPath())){
						FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + e.getPath(), eventexcelpathSub+sep+e.getPath().replace("\\", "/").substring(e.getPath().replace("\\", "/").lastIndexOf("/")));
					}
					break;
				}
				}
				
			}
			
			
			
			String fujianpath=exceldpath + sep + "附件";
			File fuDirPath = new File(fujianpath);
			if (!fuDirPath.exists()) {
				fuDirPath.mkdirs();
			}
			List<EventAttachement> eas = eventService.queryEventAttachementByEventId(event.getId());
			if(!CollectionUtils.isEmpty(eas)){
				for(EventAttachement ea:eas)
				
					FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + ea.getPath(), fujianpath+sep+ea.getPath().replace("\\", "/").substring(ea.getPath().replace("\\", "/").lastIndexOf("/")));
			}
			
			
			short hightte = (short) 16.50;
			
			row = sheet1.createRow(12);
			if(!StringUtils.isBlank(event.getRisk())){
			int rnumte = event.getRisk().length() / 23;
			if(rnumte < 1) {
				rnumte = 1;
			}
			row.setHeightInPoints(hightte * (rnumte + 1));
			}
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("主要存在风险：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getRisk()==null?"":event.getRisk());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(13);
			if(!StringUtils.isBlank(event.getLink_name())){
			 int rnumte = event.getLink_name().length() / 23;
			if(rnumte < 1) {
				rnumte = 1;
			}
			row.setHeightInPoints(hightte * (rnumte + 1));
			}
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("第三方通讯录：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getLink_name()==null?"":event.getLink_name());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(14);
			if(!StringUtils.isBlank(event.getUnit_name())){
				int rnumte = event.getUnit_name().length() / 23;
				if(rnumte < 1) {
					rnumte = 1;
				}
				row.setHeightInPoints(hightte * (rnumte + 1));
			}
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("单位名称：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getUnit_name()==null?"":event.getUnit_name());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			row = sheet1.createRow(15);
			row.setHeightInPoints((float) 81.00);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("审核意见：");
			cell.setCellStyle(dataStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue(event.getMessage());
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			

			int i = 16;
			if(replys != null && replys.size() > 0) {
			
			row = sheet1.createRow(i);
			row.setHeightInPoints((float) 20.00);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("回复人");
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(1);
			cell.setCellValue("消息内容");
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			cell = row.createCell(5);
			cell.setCellValue("回复时间");
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			i++;
			}
			short hight = (short) 16.50;
			Integer c = 1;
			for(EventReply er : replys) {
				sheet1.addMergedRegion(new CellRangeAddress(i,i,1,4));
				int rnum = er.getContent().length() / 23;
				if(rnum < 1) {
					rnum = 1;
				}
				row = sheet1.createRow(i);
				row.setHeightInPoints(hight * (rnum + 1)); 
				row.setRowStyle(rowStyle);
				cell = row.createCell(0);
				cell.setCellValue(er.getReplier());
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				
				cell = row.createCell(1);
				cell.setCellValue(er.getContent());
				cell.setCellStyle(valueStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				
				cell = row.createCell(5);
				cell.setCellValue(DateFormatter.dateToString(er.getCreate_time(), "yyyy/MM/dd hh:mm:ss"));
				cell.setCellStyle(dataStyle);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				
				String repath=exceldpath + sep + er.getReplier()+"回复附件"+DateFormatter.dateToString(er.getCreate_time(), "yyyy-MM-dd hh:mm:ss");
				File reDirPath = new File(repath);
				if (!reDirPath.exists()) {
					reDirPath.mkdirs();
				}
				if(!CollectionUtils.isEmpty(er.getEras())){
					for(EventReplyAttachement e:er.getEras()){
						FileUtils.copyFile(SettingUtils.getCommonSetting("upload.file.path") + sep + e.getPath(), repath+sep+e.getPath().replace("\\", "/").substring(e.getPath().replace("\\", "/").lastIndexOf("/")));
					}
				}
				c++;
				
				i++;
				
				
			}
			
			sheet1.addMergedRegion(new CellRangeAddress(i,i,0,5));
			
			row = sheet1.createRow(i);
			row.setHeightInPoints((float) 296.00);
			row.setRowStyle(rowStyle);
			cell = row.createCell(0);
			cell.setCellValue("传阅人:");
			cell.setCellStyle(valueStyle);
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			
			work.write(fileOut);
			fileOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		/*File zipFile = new File(fileDir+sep+"export_excel.zip");
		if(zipFile.exists()){
			zipFile.delete();
		}*/
		// 压缩文件夹并下载，下载后删除文件夹
		//FileUtils.createZip(response, path, "export_excel");
		ct = fileDir+sep+"export_excel_"+ct+".zip";
		FileUtils.createZip(ct, path);
		response.setContentType("APPLICATION/OCTET-STREAM");
		response.setHeader("Content-Disposition", "attachment; filename="
				+ "export_excel.zip");
		File zipFile = new File(ct);
		response.addHeader("Content-Length", "" + zipFile.length());
		OutputStream os = null;
		FileInputStream fis=null;
		try {
			os=response.getOutputStream();
			fis=new FileInputStream(zipFile);
			byte [] content=new byte[2048];
            int len;
            while((len=fis.read(content))!=-1){
                os.write(content,0,len);
                os.flush();
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
            try {
                if(fis!=null){
                    fis.close();
                }
                if(os!=null){
                    os.close();
                }
            }catch(IOException e){
            	e.printStackTrace(); 
            }
		}
		FileUtils.deleteDirectory(path);
		zipFile.delete();
		return null;
	}
	
	
	@RequestMapping(value = "/{id}/uploadFile", method = RequestMethod.POST)
	public void uploadFile(HttpServletRequest request,HttpServletResponse response, Model model,
			@PathVariable String id) {
		response.setContentType("text/html;charset=utf-8");
		try {
			PrintWriter out = response.getWriter();
			String path = "";
			List<String> paths = DataUtil.uploadFile(request, "moon_file");
			if (!StringUtils.isBlank(paths.get(0))) {
				path=paths.get(0);
			}
			if(!StringUtils.isBlank(path)){
				eventService.updateEventMoonPath(Integer.valueOf(id),path);
				 
				 out.print("<div>"+path+"</div>");
				//return "<div>"+path+"</div>";
			} else {
				out.print("<div>false</div>");
				//return "<div>false</div>";
			}
		} catch (Exception e) {
			e.printStackTrace();
			//return "<div>false</div>";
			try {
				response.setContentType("text/html;charset=utf-8");
				 PrintWriter out = response.getWriter();
				out.print("<div>false</div>");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	@RequestMapping(value = "/update_delay", method = RequestMethod.GET)
	public @ResponseBody String updateEventDelay(HttpServletRequest request, @RequestParam Integer id) {
		SysUsers ud = (SysUsers) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if(ud.getLevel() == 3) {
			eventService.updateEventDelayById(id);
			return "success";
		}
		return "error";
	}
}