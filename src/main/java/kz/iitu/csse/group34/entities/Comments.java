package kz.iitu.csse.group34.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_comments")
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotEmpty
    private Users author;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @NotEmpty
    private Restaurants restaurant;

    @Column(name = "comment")
    @NotEmpty
    private String comment;

    @Column(name = "postDate")
    @NotEmpty
    private Date postDate;

}
