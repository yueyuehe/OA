package cn.hm.oil.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import cn.hm.oil.domain.Lottery;



public interface LotteryDao {

	public void saveLottery(Lottery lottery);
	
	public Lottery queryLottery(Map<String,Object> param);
	
	public List<Lottery> queryLotteryList(@Param(value = "status")Integer status);
	
	public void updateLottery(@Param(value = "phone")String phone);
	
	public void clearLotteryList();
}
