package com.example.demo.Controller;

import com.example.demo.KakaoApi;
import com.example.demo.Model.Member;
import com.example.demo.Service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RestController
public class HomeController {
    private final KakaoApi kakaoApi;
    private final MemberService memberService; //몽고디비 연결할거임

    @GetMapping("/login")
    public void login() {
    }


    @GetMapping("/KakaoSignup")
    public ResponseEntity<Map<String, String>> kakaoRedirect(HttpServletRequest request){
        String code = request.getParameter("code");

        String accessToken = kakaoApi.getAccessToken(code);

        // 3. 사용자 정보 받기
        Map<String, Object> userInfo = kakaoApi.getUserInfo(accessToken);
        String email = (String)userInfo.get("email");
        String nickname = (String)userInfo.get("nickname");

        System.out.println("email = " + email);
        System.out.println("nickname = " + nickname);
        System.out.println("accessToken = " + accessToken);

        // 프론트엔드에 email을 보내주는 코드
        Map<String, String> response = new HashMap<>();
        response.put("memberEmail", email);
        response.put("memberNickname", nickname);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/KakaoSignup")
    public void KakaoSignup(@RequestBody Member member){
        System.out.println("memberemail = " + member.getMemberEmail());
        System.out.println("membernickname = " + member.getMemberNickname());
        System.out.println("memberpassword = " + member.getMemberPassword());
        System.out.println("membername = " + member.getMemberName());
        memberService.saveMember(member); //몽고 저장


    }
}




