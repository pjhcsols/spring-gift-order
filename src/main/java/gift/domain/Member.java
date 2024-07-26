package gift.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Member extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Wish> wishes = new HashSet<>();

    public Member() {
    }

    public Member(String email, String password) {
        super();
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Set<Wish> getWishes() {
        return wishes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static class Builder {
        private Long id;
        private String email;
        private String password;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Member build() {
            Member member = new Member(email, password);
            member.id = this.id;
            return member;
        }
    }
}
