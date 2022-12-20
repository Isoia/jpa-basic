package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try{
                Team team = new Team();
                team.setName("teamA");
                em.persist(team);

                Member member = new Member();
                member.setAge(10);
                member.setTeam(team);

                em.persist(member);

            String result = em.createQuery("select m.orderList From Member m",String.class)
                    .getSingleResult();


            System.out.println("result = " + result);

            tx.commit();
        } catch (Exception e){
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
