package com.example.demo.Repository;

import com.example.demo.Model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {


}
