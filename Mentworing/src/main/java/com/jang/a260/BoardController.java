package com.jang.a260;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jang.a260.mapper.MantorMapper;
import com.jang.a260.model.AttFileVO;
import com.jang.a260.model.BoardLikeVO;
import com.jang.a260.model.BoardVO;
import com.jang.a260.model.BoardViewVO;
import com.jang.a260.model.ChatVO;
import com.jang.a260.model.MemberProfileVO;
import com.jang.a260.model.MentorVO;
import com.jang.a260.model.ReplyLikeVO;
import com.jang.a260.model.ReplyVO;
import com.jang.a260.model.ReviewVO;
import com.jang.a260.model.SearchVO;
import com.jang.a260.model.User;
import com.jang.a260.model.Video;
import com.jang.a260.service.BoardService;
import com.jang.a260.service.ChatService;
import com.jang.a260.service.MantorService;
import com.jang.a260.service.UserService;

@Controller
@RequestMapping("/board")
public class BoardController {

	@Autowired
	private BoardService boardService;

	@Autowired
	private UserService userService;

	@Autowired
	private MantorService mantorService;
	
	@Autowired
	private ChatService chatService;


	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String listPage(@ModelAttribute("searchVO") SearchVO seachVO, Model model, HttpSession session)
			throws Exception {

		List<BoardVO> blist = boardService.getBoardList(seachVO);

		model.addAttribute("boardList", blist);

		StringBuffer pageUrl = boardService.getPageUrl(seachVO);
		model.addAttribute("pageHttp", pageUrl);
		model.addAttribute("user", new User());

		return "board/list";
	}

	@RequestMapping(value = "/write", method = RequestMethod.GET)
	public String boardWrite() {
		return "/board/write";
	}

	String uploadPath = "C:\\upload\\"; // file upload path ??????????????? ?????? ????????? ??????

	@RequestMapping(value = "/write", method = RequestMethod.POST)
	public String boardWriteProc(@ModelAttribute("BoardVO") BoardVO boardVO, MultipartHttpServletRequest request) {
		// ??? ?????????

		// String content = boardVO.getContent().replaceAll("\r\n", "<br />");// java??????
		// ?????? HTML???????????????
		// content = content.replaceAll("<", "&lt;");
		// content = content.replaceAll(">", "&gt;");
		// content = content.replaceAll("&", "&amp;");
		// content = content.replaceAll("\"", "&quot;");

		// boardVO.setContent(content);
		System.out.println("1");
		boardService.writerArticle(boardVO);
		System.out.println("2");
		int bno = boardVO.getBno();
		// ????????? ????????? ??? ???????????? ????????? : ?????????? xml ?????? insert keyProperty="bno"??? ????????? ?????????
		// ?????? ?????? ??????
		AttFileVO file = new AttFileVO();

		List<MultipartFile> fileList = request.getFiles("file");
		for (MultipartFile mf : fileList) {
			if (!mf.isEmpty()) {
				String originFileName = mf.getOriginalFilename(); // ?????? ?????? ???
				long fileSize = mf.getSize(); // ?????? ?????????
				file.setBno(bno);
				file.setOfilename(originFileName);
				file.setSfilename(originFileName); // ???????????? ?????? ????????? ?????? ????????? ????????????:?????? ?????? ??????
				file.setFilesize(fileSize);
				System.out.println(bno);
				System.out.println(originFileName);
				System.out.println(originFileName);
				System.out.println(fileSize);
				boardService.insertFile(file);// ???????????? ?????? ?????? ??????
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
		return "redirect:list";
	}

	@RequestMapping("/view")
	public String boardView(@RequestParam(value = "bno", required = false, defaultValue = "0") int bno,
			HttpSession session, Model model) throws Exception {

		String userId = (String) session.getAttribute("userId");
		System.out.println("view??? ????????? ????????????" + userId);
		BoardViewVO boardViewVO = new BoardViewVO(); // ????????? ?????? ???
		boardViewVO.setBno(bno);

		if (userId != null) { // ????????? ??????

			boardViewVO.setUserId(userId);
			boardViewVO.setMem_yn('y');// ??????

			if (boardService.getBoardView(boardViewVO) == 0) { // ?????? ?????? ?????? ?????? ??????????????????
				boardService.increaseViewCnt(bno); // ????????? ??????
				boardService.addBoardView(boardViewVO); // ?????? ????????????
			}
		} else { // ?????????
			boardViewVO.setUserId(session.getId()); // ?????? id ??? ?????? id ??? ??????
			boardViewVO.setMem_yn('n'); // ?????????

			if (boardService.getBoardView(boardViewVO) == 0) { // ?????? ?????? ?????? ?????? ????????? ?????? ???
				boardService.increaseViewCnt(bno); // ????????? ??????
				boardService.addBoardView(boardViewVO); // ????????? ????????????
			}
		}

		model.addAttribute("user", new User());
		BoardVO boardVO = boardService.getArticle(bno); // get selected article model
		List<ReplyVO> reply = boardService.getReplyList(bno); // ?????? ?????? ?????? ?????? ??? list
		List<AttFileVO> fileList = boardService.getFileList(bno); // ???????????? ?????? ?????? ??????-list
		model.addAttribute("board", boardVO);
		model.addAttribute("replyList", reply);
		model.addAttribute("fileList", fileList);
		return "board/view";

	}

	@RequestMapping(value = "/modify", method = RequestMethod.GET)
	public String boardModify(HttpServletRequest request, HttpSession session, Model model) {
		System.out.println("1");

		String userId = (String) session.getAttribute("userId");
		int bno = Integer.parseInt(request.getParameter("bno"));
		System.out.println(bno);
		BoardVO board = boardService.getArticle(bno);
		// <br /> tag change to new line code
		String content = board.getContent().replaceAll("<br />", "\r\n");
		board.setContent(content);
		if (!userId.equals(board.getWriterId())) {
			System.out.println(board.getWriterId() + "" + userId);
			model.addAttribute("user", new User());
			model.addAttribute("errCode", "5"); // ???????????? ????????? ??????
			model.addAttribute("bno", bno);
			return "redirect:view";
		} else {// ?????? ?????????
			System.out.println(board.getWriterId() + "" + userId);
			List<AttFileVO> fileList = boardService.getFileList(bno); // ???????????? ?????? ?????? - list
			model.addAttribute("user", new User());
			model.addAttribute("board", board);
			model.addAttribute("fileList", fileList);
			return "/board/modify";
		}
	}

	@RequestMapping(value = "/modify", method = RequestMethod.POST) // ????????? ??? ??????
	public String boardModifyProc(@ModelAttribute("Board") BoardVO board, MultipartHttpServletRequest request,
			RedirectAttributes rea) {

		System.out.println("?????? post");
		String content = board.getContent().replaceAll("\r\n", "<br />"); // java ?????? ?????? HTML???????????????
		board.setContent(content);

		boardService.updateArticle(board);
		int bno = board.getBno();
		System.out.println(bno);
		// ????????? ????????? ???????????? ??????????????? ????????????.
		String[] fileno = request.getParameterValues("fileno");

		if (fileno != null) {
			for (String fn : fileno) {

				int fno = Integer.parseInt(fn);
				String oFileName = boardService.getFileName(fno);
				String safeFile = uploadPath + oFileName;
				File removeFile = new File(safeFile);// remove disk uploaded file
				removeFile.delete();
				boardService.deleteFile(fno); // remove table uploaded file
			}

		}

		AttFileVO file = new AttFileVO();

		// ????????? ?????? ?????? ????????????
		List<MultipartFile> fileList = request.getFiles("file");
		for (MultipartFile mf : fileList) {
			if (!mf.isEmpty()) {
				String originFileName = mf.getOriginalFilename(); // ??????????????? ?????? ?????? ???
				long fileSize = mf.getSize(); // ?????? ?????????
				file.setBno(bno);
				file.setFilesize(fileSize);
				file.setOfilename(originFileName);
				file.setSfilename(originFileName);
				boardService.insertFile(file); // ???????????? ?????? ??????
				String safeFile = uploadPath + originFileName;
				try {
					mf.transferTo(new File(safeFile)); // ???????????? ?????? ??????
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		rea.addAttribute("bno", board.getBno());
		return "redirect:/board/view";
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String boarddelete(HttpServletRequest request, HttpSession session, Model model, RedirectAttributes rea)
			throws Exception {
		System.out.println("1");

		String userId = (String) session.getAttribute("userId");
		int bno = Integer.parseInt(request.getParameter("bno"));
		System.out.println("?????? bno=" + bno);
		BoardVO board = boardService.getArticle(bno);
		System.out.println(board.getWriterId());
		System.out.println(userId);
		String setView = "";
		if (userId.equals(board.getWriterId())) {
			// ?????? ??????
			List<ReplyVO> reply = boardService.getReplyList(bno);

			if (reply.size() > 0) {
				boardService.deleteReplyBybno(bno);
			}
			// ?????? ????????? ??????, ?????? ?????? ??????
			List<AttFileVO> files = boardService.getFileList(bno);
			if (files.size() > 0) {
				// ????????? ?????? ?????? ??????
				for (AttFileVO filedel : files) {
					String f_stor_all = filedel.getOfilename();
					File f = new File(session.getServletContext().getRealPath("/") + f_stor_all);
					f.delete();
				}
				boardService.deleteFileBybno(bno); // ??????????????? ?????? ?????? ??? ?????? file ?????? ??????
			}
			// board ??????
			boardService.deleteArticle(bno);
			setView = "redirect:list";

		} else {
			rea.addAttribute("errCode", "1");// it's forbidden connection
			rea.addAttribute("bno", bno);
			setView = "redirect:view";

		}
		return setView;

	}

	@RequestMapping(value = "filedown")
	@ResponseBody
	public byte[] downProcess(HttpServletResponse response, @RequestParam String fileName) throws IOException {
		File file = new File("c:/upload/" + fileName);
		byte[] bytes = FileCopyUtils.copyToByteArray(file); // SPRING 5.0 ??????
		String fn = new String(file.getName().getBytes(), "iso_8859_1");
		response.setHeader("Content-Disposition", "attachment;filename=\"" + fn + "\"");
		response.setContentLength(bytes.length);
		return bytes;
	}

	@RequestMapping("/recommend")
	public String updateRecommendCnt(HttpServletRequest request, HttpSession session, RedirectAttributes rea) {
		int bno = Integer.parseInt(request.getParameter("bno"));
		String userId = (String) session.getAttribute("userId");
		if (userId == null) {
			rea.addAttribute("bno", bno);
			rea.addAttribute("errCode", "4");
			return "redirect:/board/view";
		}
		BoardLikeVO boardLike = new BoardLikeVO();
		boardLike.setBno(bno);
		boardLike.setUserId(userId);
		BoardVO board = boardService.getArticle(bno);
		if (board.getWriterId().equals(userId)) {
			rea.addAttribute("errCode", "3"); // ?????? ?????? ?????? ??????
		} else {

			if (boardService.getBoardLike(boardLike) == 0) { // ?????? ????????? ????????? ?????????
				System.out.println(boardService.getBoardLike(boardLike));
				boardService.incrementGoodCcnt(bno);
				boardService.addBoardLike(boardLike); // ?????? ?????? ??????
			} else {
				rea.addAttribute("errCode", "2"); // ?????? ???????????? ????????? ?????? ??????
			}
		}
		rea.addAttribute("bno", bno);
		return "redirect:/board/view";
	}

	@RequestMapping("/writeReply")
	public String replyWriteProc(@ModelAttribute("reply") ReplyVO reply, RedirectAttributes rea) {
		if (reply.getContent().isEmpty()) {
			rea.addAttribute("errCode", "1");
		} else {
			String content = reply.getContent().replaceAll("<", "&lt;");
			content = reply.getContent().replaceAll(">", "&gt;");
			content = reply.getContent().replaceAll("&", "&amp;");
			content = reply.getContent().replaceAll("\"", "&quot;");
			content = reply.getContent().replaceAll("\r\n", "<br />");
			reply.setContent(content);
			boardService.writeReply(reply);
		}
		rea.addAttribute("bno", reply.getBno());
		return "redirect:view";
	}

	@RequestMapping("/deleteReply")
	public String commentDelete(HttpServletRequest request, HttpSession session, RedirectAttributes rea) {
		int rno = Integer.parseInt(request.getParameter("rno"));
		int bno = Integer.parseInt(request.getParameter("bno"));
		String userId = (String) session.getAttribute("userId");
		System.out.println("1");
		ReplyVO reply = boardService.getReply(rno);
		if (!userId.equals(reply.getWriterId())) {
			rea.addAttribute("errCode", "1");
		} else {
			boardService.deleteReply(rno);
			boardService.decrementReplayCnt(bno);
		}
		rea.addAttribute("bno", bno); // move back to the article
		return "redirect:view";
	}

	@RequestMapping("/recommandReply")
	public String RecommendRely(HttpServletRequest request, HttpSession session, RedirectAttributes rea) {
		int bno = Integer.parseInt(request.getParameter("bno"));
		int rno = Integer.parseInt(request.getParameter("rno"));
		String userId = (String) session.getAttribute("userId");
		if (userId == null) {
			rea.addAttribute("bno", bno);
			rea.addAttribute("errCode", "4");
			;
			return "redirect:/board/view";
		}
		ReplyLikeVO replyLike = new ReplyLikeVO();
		replyLike.setRno(rno);
		replyLike.setUserId(userId);
		BoardVO board = boardService.getArticle(bno);
		if (board.getWriterId().equals(userId)) {
			rea.addAttribute("errCode", "3");
		} else {
			if (boardService.getReplyLike(replyLike) == 0) {
				boardService.incReplyGoodCnt(rno);
				boardService.addReplyLike(replyLike);
			} else {
				rea.addAttribute("errCode", "2");
			}
		}
		rea.addAttribute("bno", bno);
		return "redirect:/board/view";
	}

	@RequestMapping(value = "/Mentor_Review", method = RequestMethod.GET)
	public String Mentor_Review_GET(@ModelAttribute("searchVO") SearchVO seachVO, HttpServletRequest request,
			Model model, RedirectAttributes rea) throws Exception {

		StringBuffer pageUrl = boardService.getPageUrl2(seachVO);

		List<ReviewVO> review = boardService.getMentorReview(seachVO);

		List<MentorVO> mentor = mantorService.mentorList();

		List<User> member = userService.selectAlluser();

		model.addAttribute("pageHttp", pageUrl);
		model.addAttribute("mentor", mentor);
		model.addAttribute("member", member);
		model.addAttribute("user", new User());
		model.addAttribute("review", review);
		return "./Mentor_Review";

	}

	@RequestMapping(value = "/Mentor_Review", method = RequestMethod.POST)
	public String Mentor_Review(@ModelAttribute ReviewVO review, HttpServletRequest request, Model model,
			RedirectAttributes rea) throws Exception {

		String grade = request.getParameter("star");// ?????? ??????????????????
		int mno = Integer.parseInt(request.getParameter("mno"));
		MentorVO mentor = mantorService.selectmentorview(mno);
		review.setMentorprofile(mentor.getProfile());
		review.setGrade(Integer.parseInt(grade));
		String content=review.getContent().replace("\r\n","<br>");
		review.setContent(content);
		System.out.println(review.getContent().replace("\r\n","<br>"));
		System.out.println(review.getMno());
		System.out.println(review.getWriterid());
		System.out.println(review.getGrade());
		int result = boardService.insertMentorReview(review);

		if (result == 1) {
			rea.addAttribute("mno", mno);
			rea.addAttribute("review_check", "1");
		}

		return "redirect:../Mentor/Mentorview";

	}


}
