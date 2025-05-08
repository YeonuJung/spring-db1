package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            // 트랜잭션 시작
            con.setAutoCommit(false);

            // 비즈니스 로직
            bizLogic(fromId, toId, money, con);

            // 성공시 커밋
            con.commit();
        } catch (Exception e) {
            // 실패시 롤백
            con.rollback();
            throw new IllegalStateException(e);
        } finally {
            if (con != null) {
                release(con);
            }
        }
    }

    private void bizLogic(String fromId, String toId, int money, Connection con) throws SQLException {
        // 비즈니스 로직 수행
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void release(Connection con) {
        try {
            // true로 바꿔주지 않으면 이후에 반환시에
            // 오토커밋이 꺼진 상태로 반환되기 때문에 커넥션 풀을 고려
            con.setAutoCommit(true);
            con.close();
        } catch (Exception e) {
            log.info("error", e);
        }
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId()
                    .equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생!");
        }
    }
}
