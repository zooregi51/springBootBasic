package hello.hellospring.service;

import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    Logger logger = LoggerFactory.getLogger((MemberService.class));

    /**
     * 회원가입
     */
    public Long join(Member member){
        validateDuplicateMember(member); // 중복 회원 검사
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
                });
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> findOne(Long memberId){
        return memberRepository.findById(memberId);
    }

    public String makeJwt(Member member){
        Date now = new Date();

        String result = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더를 jwt 타입으로 설정
                .setIssuer("admin") // 클레임의 토큰 발행자 설정
                .setIssuedAt(now) // 토큰 발행시간 설정, Date 타입만 가능
                .setExpiration(new Date(now.getTime() + Duration.ofMinutes(30).toMillis())) // 토큰 만료시간 설정
                .claim("name", member.getName())
                .signWith(SignatureAlgorithm.HS256, "secret")
                .compact();

        //System.out.println("jwt = " + result);
        logger.info("{}", result);
        return result;
    }

    public void checkClaim(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey("secret")
                .parseClaimsJws(jwt)
                .getBody();

        Date expiration = claims.getExpiration();
        logger.info(expiration.toString());
        //System.out.println(expiration);

        String data = claims.toString();
        //System.out.println(data);
        logger.info(data);
    }
}