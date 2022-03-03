package hexlet.code.domain;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

import io.ebean.annotation.WhenCreated;
import lombok.Getter;

@Getter
@Entity
public class Url extends Model {

    @Id
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    public Url() {

    }

    public Url(String nameUrl) {
        this.name = nameUrl;
    }

}
