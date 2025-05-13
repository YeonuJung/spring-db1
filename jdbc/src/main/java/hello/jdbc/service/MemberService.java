package hello.jdbc.service;

public interface MemberService {
    void accountTransfer(String fromId, String toId, int money);
}
