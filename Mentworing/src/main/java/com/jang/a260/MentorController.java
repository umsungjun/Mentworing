package com.jang.a260;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jang.a260.model.BasketVO;
import com.jang.a260.model.BoardVO;
import com.jang.a260.model.ChatVO;
import com.jang.a260.model.IncomeVO;
import com.jang.a260.model.MemberProfileVO;
import com.jang.a260.model.Mentee_list_VO;
import com.jang.a260.model.MentorApprove;
import com.jang.a260.model.MentorVO;
import com.jang.a260.model.NotificationMemberVO;
import com.jang.a260.model.ReviewVO;
import com.jang.a260.model.SearchVO;
import com.jang.a260.model.User;
import com.jang.a260.model.Video;
import com.jang.a260.model.Video_recordVO;
import com.jang.a260.model.menteelist_notification;
import com.jang.a260.model.paymentDetailVO;
import com.jang.a260.model.paymentVO;
import com.jang.a260.service.BasketService;
import com.jang.a260.service.BoardService;
import com.jang.a260.service.ChatService;
import com.jang.a260.service.MantorService;
import com.jang.a260.service.MenteeService;
import com.jang.a260.service.UserService;
import com.jang.a260.service.VideoService;
import com.jang.a260.utils.WebSocketConfig;

@Controller
@RequestMapping("/Mentor")
public class MentorController {

	@Autowired
	private MantorService mantorService;

	@Autowired
	private UserService userService;

	@Autowired // @Resource(name="userService")
	private MenteeService menteeService;

	@Autowired
	private BasketService basketService;
	
	@Autowired
	private BoardService boardService;
	
	@Autowired
	private VideoService videoService;



	@Autowired
	private ChatService chatService;

	String uploadPath = "C:\\upload\\"; // file upload path ??????????????? ?????? ????????? ??????

	@RequestMapping(value = "/Mentor_list", method = RequestMethod.GET)
	public String Mentor_list(@ModelAttribute("searchVO") SearchVO searchVO, Model model, HttpSession session)
			throws Exception {
		List<NotificationMemberVO> notificationmember = new ArrayList<NotificationMemberVO>();
		List<MentorVO> mentorlist = mantorService.mentorList();

		List<User> userlist = userService.selectAlluser();
		String userId = (String) session.getAttribute("userId");

		model.addAttribute("mentorlist", mentorlist);
		model.addAttribute("userlist", userlist);
		model.addAttribute("user", new User());

		return "/Mentor/Mentor_list";
	}

	@RequestMapping(value = "/Mentorview", method = RequestMethod.GET)
	public String Mentorview(HttpServletRequest request, Model model, HttpSession session) throws Exception {
		List<Video> videolist = mantorService.videoList();// ?????????????????????
		List<ChatVO> chatvo = null;
		int count = 0;
		int mno = 0;
		int roomno=0;
		User user = new User();
		MentorVO mentor = new MentorVO();
		String paymentIdsuccess = null; // ?????? ????????? ??????
		String review_check = null;
		String name = (String) session.getAttribute("userName");
		String userId = (String) session.getAttribute("userId");

		mno = Integer.parseInt(request.getParameter("mno"));
		User u=userService.getUser(userId);
		
		
		review_check = request.getParameter("review_check");
		mentor = mantorService.selectmentorview(mno);

		if (userId != null) {
			MemberProfileVO memberprofile = userService.getUserProfile(userId); // ????????? ????????? ????????? ????????? ?????????

			if (memberprofile != null) {
				model.addAttribute("memberprofile", memberprofile);
			} else {
				model.addAttribute("memberprofile", "");
			}

			int no = mentor.getNo();
			System.out.println(no+"zzz"+u.getNo()+"?????? ?????????"+roomno);
			roomno=no+u.getNo();
			user = userService.getUserformento(no);

			// ????????? ???????????? ??????????????????

			int mentormno = mentor.getMno();

			if (userId != null) {
				String[] paymentId = mantorService.getpaymentcheck(userId, mentormno);

				chatvo = chatService.getChatList();

				for (int i = 0; i < paymentId.length; i++) {
					if (paymentId[i].equals(userId)) {// ???????????? ????????? ???????????? ???????????? ????????? ????????? ??? ???????????? ????????? ?????????
						count += 1;
						paymentIdsuccess = paymentId[i];
					}

				}

				model.addAttribute("paymentId", paymentIdsuccess);
				model.addAttribute("chatVO", chatvo);// ???????????????
			}
		}
		
		MentorVO mentorinfo=mantorService.selectmentorview(mno);
		User mentorinformation=userService.getUserformento(mentorinfo.getNo());

		System.out.println(count);
		System.out.println(paymentIdsuccess);
		model.addAttribute("videolist", videolist);// ????????? ?????????
		model.addAttribute("mno", mno); // ???????????? ????????? ???????????????
		model.addAttribute("name", name);// ????????????
		model.addAttribute("userId", userId);// ???????????????
		model.addAttribute("review_check", review_check);
		model.addAttribute("mentorname", mentorinformation.getName()); // ????????????
		model.addAttribute("mentor", mentor);// ???????????????
		model.addAttribute("mentorprofile", "");
		model.addAttribute("roomno",roomno);
		session.setAttribute("roomno",roomno);
		model.addAttribute("user", new User());
		System.out.println("????????? ??????:" + user.getName());
		System.out.println("????????? ??????:" + name);
		System.out.println(review_check);
		return "/Mentor/Mentorview";
	}

	@RequestMapping(value = "/Mentorview", method = RequestMethod.POST)
	public String MentorviewTobasket(HttpServletRequest request, Model model, HttpSession session) throws Exception {

		String userId = (String) session.getAttribute("userId");

		String[] value = request.getParameterValues("vno");
		int[] intvalue = new int[value.length];

		for (int i = 0; i < value.length; i++) {
			intvalue[i] = Integer.parseInt(value[i]);
			BasketVO basket = new BasketVO();
			Video video = mantorService.getvideoview(intvalue[i]);
			basket.setPrice(video.getVideoprice());
			basket.setTitle(video.getVideoname());
			basket.setUserId(userId);
			basket.setVno(intvalue[i]);
			basket.setBfilename(video.getVideofile());

			basketService.insertbasket(basket);
			System.out.println(intvalue[i] + "????????????");
		}
		return "redirect:/basket/mainbasket";
	}

	@RequestMapping(value = "/mente_list", method = RequestMethod.GET)
	public String mente_list(@ModelAttribute("searchVO") SearchVO searchVO, HttpSession session,
			HttpServletRequest request, Model model) {

		
		//?????????
		
		
		//????????? ?????? ????????? ????????????
		
		List<Video_recordVO> video_record=videoService.select_all_vieow_record();
		
		
		model.addAttribute("video_record",video_record);
		
		List<Mentee_list_VO> mentee_list=mantorService.mentee_list();
		
		model.addAttribute("mentee_list",mentee_list);
		
		//
		int sumMessage=0; //??? ????????? ??????
		
		List<ChatVO> chatvo = null;
		List<User> userlist = userService.selectAlluser();
		String userId = (String) session.getAttribute("userId");
		String name = (String) session.getAttribute("userName");

		User user = userService.getUser(userId); // ???????????? ???????????? ?????????????????? ????????? ?????????
		int userNo = user.getNo();
		int mno = mantorService.UsernoToMentormno(userNo); // ???????????? ????????? ???????????? ??????

		StringBuffer pageUrl = mantorService.getPageUrl(searchVO);
		model.addAttribute("pageHttp", pageUrl);
		List<paymentDetailVO> paymentList = mantorService.getpaymentdetailList(searchVO);

		// ???????????????????????????
		List<MemberProfileVO> memberProfile = userService.UserProfileList();
		MentorVO mentor = mantorService.selectmentorview(mno);
		List<Video> video = mantorService.videoList();

		int no = mentor.getNo();

		// ????????? ???????????? ??????????????????
		int mentormno = mentor.getMno();

		chatvo = chatService.getChatList();
		model.addAttribute("chatVO", chatvo);// ???????????????

		model.addAttribute("video", video);
		model.addAttribute("memberProfile", memberProfile);// ?????? ????????????
		model.addAttribute("mentorname", user.getName()); // ????????????
		// ?????? end
		model.addAttribute("userlist", userlist);
		model.addAttribute("mno", mno);
		model.addAttribute("mentor", mentor);
		model.addAttribute("mentorprofile", mentor.getProfile());
		model.addAttribute("paymentlist", paymentList);
		model.addAttribute("user", new User());
		model.addAttribute("name", name);

		// ????????? ?????? ?????? ????????????

		String[] paymentuserId = mantorService.getPaymentDetail_list();

		ArrayList<menteelist_notification> list = new ArrayList<menteelist_notification>();
		for (int i = 0; i < paymentuserId.length; i++) {
			User paymentuser = userService.getUser(paymentuserId[i]);
			int roomno = paymentuser.getNo() + userNo;
			int notificationnumber = chatService.Chatmessage2Notification(roomno, paymentuser.getName(), "X");
			System.out.println(paymentuser.getName() + "?????? ??????????????????" + notificationnumber);
			sumMessage+=notificationnumber;
			list.add(new menteelist_notification(paymentuser.getName(), notificationnumber, roomno));

		}
		
		session.setAttribute("sumMessage", sumMessage);
		
		model.addAttribute("sumMessage",sumMessage); //??? ????????? ??????
		model.addAttribute("list", list);

		return "/Mentor/mente_list";
	}

	@ResponseBody
	@RequestMapping(value = "/mente_list_getchatname", method = RequestMethod.GET)
	public List<ChatVO> mente_list_getchatname(HttpServletRequest request, Model model, HttpSession session)
			throws Exception {

		// ????????? ????????? ?????? ??????
		int notificationnumber = 0; // ?????? ??????????????? ??????
		int notificationsum = 0;
		int menteelist_roomno = 0;
		List<NotificationMemberVO> notificationmember = new ArrayList<NotificationMemberVO>();

		String userId = (String) session.getAttribute("userId");

		// json??? ?????? ??????

		Gson gson = new Gson();
		JsonObject object = new JsonObject();
		int roomno = 0;
		List<ChatVO> chatvo = null;

		String mysession = (String) session.getAttribute("userName"); // ?????? ???????????? ????????????
		String name = request.getParameter("name");// ????????? ????????? ????????? ?????? ????????? ????????????
		if (name != null) {
			menteelist_roomno = Integer.parseInt(request.getParameter("roomno"));
		}

		String mentorviewmentor = request.getParameter("mentorname");// ????????? ????????? ????????? ???????????? ????????? ????????? ??????
		System.out.println("?????????:" + menteelist_roomno);
		// ????????? ???????????? ????????? ?????????????????? ????????? ????????? ????????????
		if (mentorviewmentor == null) {
			System.out.println("????????????" + mysession.toString());
			System.out.println("????????????" + name.toString());
			String[] user = userService.selectchattingname(mysession.toString());
			String[] yourName = userService.selectchattingname(name.toString());
			for (int i = 0; i < user.length; i++) {
				roomno = Integer.parseInt(user[i]) + Integer.parseInt(yourName[i]);

			}
			chatService.updateChatmessage(roomno, "O", name.toString());
			chatService.updateChatmessage2(menteelist_roomno, "O", name);
			chatvo = chatService.getChatListforuserId(roomno);
		} else {// ??????????????? ???????????? ??????
			System.out.println("????????????" + mysession.toString());
			System.out.println("????????????" + mentorviewmentor.toString());
			String[] user = userService.selectchattingname(mysession.toString());
			String[] yourName = userService.selectchattingname(mentorviewmentor.toString());
			for (int i = 0; i < user.length; i++) {
				roomno = Integer.parseInt(user[i]) + Integer.parseInt(yourName[i]);

			}
			chatService.updateChatmessage(roomno, "O", mentorviewmentor.toString());
			chatvo = chatService.getChatListforuserId(roomno);
		}

		// ????????? ???????????? ???????????? ???????????? ??????

		if (userId != null) {
			User user2 = userService.getUser(userId); // ???????????? ???????????? ?????????????????? ????????? ?????????
			int userNo = user2.getNo();

			// ????????? ????????? ???????????? ?????? ????????? ???????????? ?????????
			int[] mentornumber = menteeService.getpaymentdetail(userId);
			for (int i = 0; i < mentornumber.length; i++) {
				NotificationMemberVO member = new NotificationMemberVO();
				int no = mantorService.MentormnoToUserno(mentornumber[i]);
				User mentoruser = userService.getUserformento(no);

				int roomno2 = (userNo + no);
				int mno = mantorService.UsernoToMentormno(no);
				MentorVO mentor = mantorService.selectmentorview(mno);

				System.out.println(mentoruser.getName() + "" + roomno + "X");
				notificationnumber = chatService.ChatmessageNotification(roomno2, mentoruser.getName(), "X");
				System.out.println("???????????? ?????????:" + notificationnumber);
				System.out.println(roomno);
				System.out.println(roomno2);
				if (roomno == roomno2) {
					member.setName(mentoruser.getName());
					member.setNotificationnumber(0);
					member.setMemberprofile(mentor.getProfile());
					member.setMno(mentor.getMno());
					notificationmember.add(member);
				} else {
					member.setName(mentoruser.getName());
					member.setNotificationnumber(notificationnumber);
					member.setMemberprofile(mentor.getProfile());
					member.setMno(mentor.getMno());
					notificationmember.add(member);
				}

				notificationsum = notificationsum + notificationnumber;
				System.out.println("????????? ???" + notificationsum);

			}
			// ?????? ?????????
			session.setAttribute("checkMessage", notificationsum);
			session.setAttribute("notificationmember", notificationmember);

		}

		//

		if (name != null || mentorviewmentor != null) {
			object.addProperty("msg", "true");
			System.out.println(gson.toJson(object).toString());
			return chatvo;
		} else {
			object.addProperty("msg", "false");
			System.out.println(gson.toJson(object).toString());
			return chatvo;
		}

	}

	@RequestMapping(value = "/Mentor_Mypage", method = RequestMethod.GET)
	public String Mentor_Mypage(@ModelAttribute("searchVO") SearchVO searchVO, Model model, HttpSession session)
			throws Exception {

		String userId = (String) session.getAttribute("userId");
		User loginUser = this.userService.getUser(userId);
		int mno = mantorService.UsernoToMentormno(loginUser.getNo());
		MentorVO mentorprofile = mantorService.selectmentorview(mno);
		System.out.println(mentorprofile.getProfile());
		if (userService == null) {
			model.addAttribute("userId", "");
			model.addAttribute("msgCode", "?????????????????? ??????????????????"); // ?????????????????? ?????????

			model.addAttribute("user", new User());
			return "/Mentor/Mentor_Mypage";
		} else {
			model.addAttribute("user", loginUser);
			model.addAttribute("memberprofile", mentorprofile.getProfile());
			return "/Mentor/Mentor_Mypage";
		}

	}

	@RequestMapping(value = "/mentee_Mypage", method = RequestMethod.POST)
	public String Mentor_Mypage_Post(@ModelAttribute User user, Model model, HttpSession session,
			MultipartHttpServletRequest requestt, HttpServletRequest request) throws Exception {

		String userId = (String) session.getAttribute("userId");
		String name = (String) session.getAttribute("userName");
		User loginUser = this.userService.getUser(userId);
		int mno = mantorService.UsernoToMentormno(loginUser.getNo());
		MentorVO mentorprofile = mantorService.selectmentorview(mno);

		if (userId != null) { // ?????????????????? ????????? ???????????????????????? ??????

			Main main = new Main();
			File file = new File(uploadPath + mentorprofile.getProfile());

			String key = mentorprofile.getProfile();
			// String copyKey = "img/my-img-copy.img";

			// upload ????????????.

			// AWS??? ?????? ?????? ????????????
			main.delete(key);

			List<MultipartFile> fileList = requestt.getFiles("profile");
			for (MultipartFile mf : fileList) {
				if (!mf.isEmpty()) {
					String originFileName = mf.getOriginalFilename(); // ?????? ?????? ???
					long fileSize = mf.getSize(); // ?????? ?????????
					mentorprofile.setProfile(originFileName);
			
					String safeFile = uploadPath + originFileName; // ???????????? ?????? ??????
					try {
						mf.transferTo(new File(safeFile)); // ???????????? ?????? ??????
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			File file2 = new File(uploadPath + mentorprofile.getProfile());

			String key2 = mentorprofile.getProfile();
			// String copyKey = "img/my-img-copy.img";

			// upload ????????????.
			main.upload(file2, key2);
			mantorService.updatementorprofile(mentorprofile);

		}

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashPass = passwordEncoder.encode(user.getPass());
		user.setPass(hashPass);

		user.setBirthday(loginUser.getBirthday());

		if (this.userService.updateUser(user) != 0) {
			user.setPass("");
			model.addAttribute("msgCode", "????????? ??????????????? ?????????????????????");
			System.out.println("????????????????????????");
			model.addAttribute("user", user);
			return "/Mentor/Mentor_Mypage";
		} else {
			model.addAttribute("msgCode", "????????? ??????????????? ?????????????????????");
			System.out.println("????????????????????????");
			return "/Mentor/Mentor_Mypage";
		}
	}

	@RequestMapping(value = "/Mentor_Mypage_Chart", method = RequestMethod.GET)
	public String Mentor_Mypage_chart(@ModelAttribute("searchVO") SearchVO searchVO, Model model, HttpSession session)
			throws Exception {
		String userId = (String) session.getAttribute("userId");
		String name = (String) session.getAttribute("userName");
		System.out.println(userId);
		User loginUser = this.userService.getUser(userId);
		int mno = mantorService.UsernoToMentormno(loginUser.getNo());

		return "/Mentor/Mentor_Mypage_chart";

	}

	@RequestMapping(value = "incomeList", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")

	public @ResponseBody String incomeList(HttpSession session, Model model) {

		String userId = (String) session.getAttribute("userId");
		String name = (String) session.getAttribute("userName");
		User loginUser = this.userService.getUser(userId);
		int mno = mantorService.UsernoToMentormno(loginUser.getNo());

		Gson gson = new Gson();

		List<IncomeVO> list = mantorService.selectChartsumprice(mno, "2022-01-01", "2022-12-31");

		System.out.println(list);

		return gson.toJson(list);

	}

	@RequestMapping(value = "chartsearch", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")

	public @ResponseBody String chartsearch(HttpSession session, Model model, HttpServletRequest request) {

		String start_date = request.getParameter("start_date");
		String end_date = request.getParameter("end_date");

		System.out.println(start_date);
		System.out.println(end_date);

		String userId = (String) session.getAttribute("userId");
		String name = (String) session.getAttribute("userName");
		User loginUser = this.userService.getUser(userId);
		int mno = mantorService.UsernoToMentormno(loginUser.getNo());

		Gson gson = new Gson();

		List<IncomeVO> list = mantorService.selectChartsumprice(mno, start_date, end_date);

		System.out.println(list);

		return gson.toJson(list);

	}

	@RequestMapping(value = "/chat_multipartUpload", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String chat_multipartUpload(MultipartHttpServletRequest request) throws Exception {

		Gson gson = new Gson();
		JsonObject object = new JsonObject();

		List<MultipartFile> fileList = request.getFiles("file");

		File fileDir = new File(uploadPath);
		System.out.println("ajax chat_multipartupload");
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		System.out.println("ajax chat_multipartupload2");
		long time = System.currentTimeMillis();

		for (MultipartFile mf : fileList) {

			String originFileName = mf.getOriginalFilename(); // ?????? ?????? ???
			System.out.println(originFileName);
			String safeFile = uploadPath + originFileName; // ???????????? ?????? ??????
			System.out.println(safeFile);

			try {
				// ????????????
				System.out.println("????????????");
				mf.transferTo(new File(uploadPath, originFileName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (fileList != null) {
			object.addProperty("msg", "false");
			return gson.toJson(object).toString();
		} else {
			object.addProperty("msg", "true");
			return gson.toJson(object).toString();
		}

	}

	@RequestMapping(value = "/Mentorview_Review", method = RequestMethod.GET)
	public String Mentorview_Review(@ModelAttribute("searchVO") SearchVO seachVO, HttpServletRequest request,
			Model model, RedirectAttributes ream, HttpSession session) throws Exception {

		
		List<ChatVO> chatvo = null;
		int count = 0;
		int mno = 0;
		User user = new User();
		MentorVO mentor = new MentorVO();
		String paymentIdsuccess = null; // ?????? ????????? ??????
		String review_check = null;
		String name = (String) session.getAttribute("userName");
		String userId = (String) session.getAttribute("userId");

		mno = Integer.parseInt(request.getParameter("mno"));
		review_check = request.getParameter("review_check");
		mentor = mantorService.selectmentorview(mno);
		
		List<ReviewVO> mentorreview=boardService.Mentorview_getMentorReview(mno);

		if (userId != null) {
			MemberProfileVO memberprofile = userService.getUserProfile(userId); // ????????? ????????? ????????? ????????? ?????????

			if (memberprofile != null) {
				model.addAttribute("memberprofile", memberprofile);
			} else {
				model.addAttribute("memberprofile", "");
			}

			int no = mentor.getNo();
			user = userService.getUserformento(no);

			// ????????? ???????????? ??????????????????

			int mentormno = mentor.getMno();

			if (userId != null) {
				String[] paymentId = mantorService.getpaymentcheck(userId, mentormno);

				chatvo = chatService.getChatList();

				for (int i = 0; i < paymentId.length; i++) {
					if (paymentId[i].equals(userId)) {// ???????????? ????????? ???????????? ???????????? ????????? ????????? ??? ???????????? ????????? ?????????
						count += 1;
						paymentIdsuccess = paymentId[i];
					}

				}

				model.addAttribute("paymentId", paymentIdsuccess);
				model.addAttribute("chatVO", chatvo);// ???????????????
			}
		}

		System.out.println(count);
		System.out.println(paymentIdsuccess);
		model.addAttribute("mentorreview",mentorreview);
		model.addAttribute("mno", mno); // ???????????? ????????? ???????????????
		model.addAttribute("name", name);// ????????????
		model.addAttribute("userId", userId);// ???????????????
		model.addAttribute("review_check", review_check);
		model.addAttribute("mentorname", user.getName()); // ????????????
		model.addAttribute("mentor", mentor);// ???????????????
		model.addAttribute("mentorprofile", "");
		model.addAttribute("user", new User());
		model.addAttribute("mentorreview",mentorreview);
		System.out.println("????????? ??????:" + user.getName());
		System.out.println("????????? ??????:" + name);
		System.out.println(review_check);

		return "../views/Mentor/Mentorview_Review";

	}

}
