package com.jang.a260.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.jang.a260.model.CourseVO;
import com.jang.a260.model.User;
import com.jang.a260.model.Video_recordVO;
import com.jang.a260.model.Videoview_videolist;
import com.jang.a260.model.best_courseVO;



@Mapper
public interface VideoMapper {

	List<CourseVO> course_list();
	
	List<best_courseVO>order_best_course();
	
	int video_record_go(Video_recordVO video_record);
	
	String get_video_record(@Param("vno") int vno, @Param("no") int no);
	
	int update_video_record(Video_recordVO video_record);
	
	List<Video_recordVO> List_video_record(int no);
	
	Video_recordVO get_video_record_DTO(@Param("vno") int vno, @Param("no") int no);
	
	List<Videoview_videolist> videoview_get_videolist(String userId);
	
	List<best_courseVO> vidoeview_get_payment_count();
	
	List<Video_recordVO>select_all_vieow_record();
}
