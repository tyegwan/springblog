package com.tenco.blog.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@Table(name = "user_tb")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    // 사용자명 중복 방지를 위한 유니크 제약 조건 설정
    @Column(unique = true)
    private String username;

    private String password;
    private String email;
    // 엔티티가 영속화 될 때 자동으로 현재 시간을 주입해라 pc -> db
    @CreationTimestamp
    private Timestamp createdAt;

    // User 테이블에는 이미지 파일명만 저장할 예정 (실제 데이터는 내 서버 컴퓨터 로컬에 저장할 예정)
    @Column(nullable =  true) // null 허용, 기본값
    private String profileImage;  // 프로필 이미지는 선택 사항(회원 가입 시)

    /**
     * User (1) : UserRole (N) 연관 관계를 정의 핰
     *
     * 1. @OneToMany + @JoinColumn(name = "user_id")
     * - User 가 UserRole 리스틀 관리합니다
     * 실제 DB user_role_tb 테이블에 FK 컬럼은 user_id 명이 user_role_tb 생성된다.
     *
     * 2. CascadeType.ALL (운명 공동체)
     * Java 기준에서 User 저장하면 Role 도 자동 저장되고 User 삭제하면 가지고 있던
     *
     *
     *
     *3. orphanRemoval (리스트와 DB를 동기화)
     *  DB 에서 실제 delete 쿼리가 발생 됩니다.
     *
     *  4. fetch = FetchType.EAGER (특별취급)
     *  데이터 양이 얼마 되지 않습니다. 그래서 한번에 데이터를 채워서 가지고 오는것이
     *  편리하다.
     */

    // User : UserRole 연관관계를 단방향 1 : N 구조 설계
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")

    private List<UserRole> roles;

    @Builder
    public User(Integer id, String username, String password,
                String email, Timestamp createdAt,
                String profileImage) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.profileImage = profileImage;
    }

    // 편의 기능 추가 - 회원 정보 수정
    public void update(UserRequest.UpdateDTO updateDTO, String newProfileImageFileName) {
        this.password = updateDTO.getPassword();
        this.profileImage = newProfileImageFileName;
    }



    // User 엔티티에 권한 관련 편의 기능 만들어 보기
    // Role.ADMIN, Role.USER
    public void addRole(Role role) {

//        new UserRole(1,Role.USER);
        //this.roles.get(0) = new UserRole(1, Role.USER);
        this.roles.add(UserRole.builder().role(role).build());
    }

    // boolean isAdmin = user.haRole(Role.ADMIN);
    public boolean hasRole(Role role) {

        if (this.roles == null || this.roles.isEmpty()) {
            return false;
        }

        for (UserRole userRole: this.roles) {
            if (userRole.getRole() == role) {
                return  true;
            }
        }
        return  false;
    }

    // 관리자 여부 확인 편의 메서드 - 머스태치에서 is 생략하고 admin으로 접근 가능합니다.
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public String getRoleDisplay() {
        return isAdmin() ? "ADMIN" : "USER";
    }

}
