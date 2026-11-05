import com.mdk.cosmetic.UnoTargetingRules;
import java.util.List;

public class UnoRulesTest {
    static final class P {
        final String name;
        double hp, distance;
        boolean eligible=true;
        P(String name,double hp,double distance) {this.name=name;this.hp=hp;this.distance=distance;}
    }
    static P select(P... pp) {
        return UnoTargetingRules.select(List.of(pp), p->p.distance*p.distance, p->p.hp, p->p.eligible, 50.0*50.0);
    }
    static void check(boolean cond,String scenario) {
        if (!cond) throw new AssertionError("FAILED " + scenario);
        System.out.println("PASS " + scenario);
    }
    public static void main(String[] args) {
        P a=new P("near",20,3), b=new P("far",20,15);
        check(select(a,b)==a,"nearest normal target");
        b.hp=3.0;
        check(select(a,b)==b,"weak beats closer full HP");
        P c=new P("weak-closer",1.0,10);
        check(select(a,b,c)==c,"closest weak among multiple weak");
        b.hp=4.0;c.hp=4.0;
        check(select(a,b,c)==a,"at exactly two hearts not weak");
        b.hp=3.99;
        check(select(a,b)==b,"strictly below two hearts is weak");
        b.distance=50.1;
        check(select(a,b)==a,"outside 50 block radius ignored");
        b.distance=49.99;
        check(select(a,b)==b,"inside 50 block radius chosen");
        b.eligible=false;
        check(select(a,b)==a,"creative or spectator filtered");
        a.eligible=false;
        check(select(a,b)==null,"no eligible players returns null");
        a.eligible=true; b.eligible=true; b.hp=3.0;
        b.distance=2;
        check(select(a,b)==b,"target switches instantly when HP changes");
        b.hp=10;b.distance=60;
        check(select(a,b)==a,"target returns to nearest after healing/out of range");
        System.out.println("11 target cases passed");
    }
}
