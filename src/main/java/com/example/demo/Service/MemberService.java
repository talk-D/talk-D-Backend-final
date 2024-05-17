package com.example.demo.Service;

import com.example.demo.Model.Member;
import com.example.demo.Repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    //회원가입 회원 저장

    @Autowired
    private MemberRepository memberRepository;

    public void saveMember(Member member) {
        memberRepository.save(member);
    }
}
