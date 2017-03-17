package cn.hm.oil.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hm.oil.dao.BasePerformanceMeasureDao_2016;
import cn.hm.oil.dao.BasePipeLineDao;
import cn.hm.oil.domain.BasePipeline;
import cn.hm.oil.domain.BaseReceiver;
import cn.hm.oil.domain.PerformanceMeasureDetail;
import cn.hm.oil.domain.PerformanceMeasure_2016;
import cn.hm.oil.domain.SysUsers;
import cn.hm.oil.service.BasePMeasureService_2016;
import cn.hm.oil.util.AbstractModuleSuport;
import cn.hm.oil.util.PageSupport;

@Service(value="basePMeasureServiceImpl_2016")
public class BasePMeasureServiceImpl_2016 extends AbstractModuleSuport implements BasePMeasureService_2016{

	@Autowired
	private BasePerformanceMeasureDao_2016 basePerformanceMeasureDao;
	
	@Autowired
	private BasePipeLineDao basePipeLineDao;
	
	@Override
	public List<BasePipeline> queryPipeLineByUser(Integer admin) {
		// TODO Auto-generated method stub
		return basePipeLineDao.queryPipeLineByUser(admin);
	}

	@Override
	public List<BaseReceiver> queryLeaderList() {
		// TODO Auto-generated method stub
		return basePipeLineDao.queryLeaderList();
	}

	@Override
	public PerformanceMeasure_2016 queryLastPMeasureByUserId(Integer id, Integer pl_id) {
		// TODO Auto-generated method stub
		return basePerformanceMeasureDao.queryLastPMeasureByUserId(id,pl_id);
	}

	@Override
	public void savePMeasure(PerformanceMeasure_2016 pp, List<PerformanceMeasureDetail> ppdList) {
		// TODO Auto-generated method stub
		if(pp.getId() != null && pp.getId().intValue() > 0) {
			basePerformanceMeasureDao.updatePerformanceMeasure(pp);
			basePerformanceMeasureDao.deletePerformanceMeasureDetailByPMid(pp.getId());
			} 
		else {
			basePerformanceMeasureDao.insertPerformanceMeasure(pp);
		}
		
		for (PerformanceMeasureDetail ppd : ppdList){
			ppd.setPm_id(pp.getId());
			basePerformanceMeasureDao.insertPerformanceMeasureDetail(ppd);
		}
	}

	@Override
	public PerformanceMeasure_2016 queryPMeasureById(Integer id) {
		// TODO Auto-generated method stub
		return basePerformanceMeasureDao.queryPMeasureById(id);
	}

	@Override
	public List<BasePipeline> queryPipeLineByAdminPMeasure(Map<String, Object> param) {
		// TODO Auto-generated method stub
		if(param.containsKey("all"))
			return basePerformanceMeasureDao.queryPipeLineByAdminPMeasure(param);
		return basePipeLineDao.queryPipeLine(param);
	}

	@Override
	public List<SysUsers> queryUsers(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return basePerformanceMeasureDao.queryUsers(param);
	}

	@Override
	public List<PerformanceMeasure_2016> queryPMeasures(Map<String, Object> param, PageSupport ps) {
		// TODO Auto-generated method stub
		if (ps == null) {
			return basePerformanceMeasureDao.queryPMeasures(param);
		} else {
			return this.getListPageSupportByManualOperationAutoCount("cn.hm.oil.dao.BasePerformanceMeasureDao_2016.queryPMeasures",param, ps);
		}
	}

	@Override
	public void updatePerformanceMeasureVerifyStatus(Integer id, Integer verifier, Integer status, String verify_desc) {
		// TODO Auto-generated method stub
		basePerformanceMeasureDao.updatePerformanceMeasureVerifyStatus(id, verifier, status, verify_desc);
	}

}
