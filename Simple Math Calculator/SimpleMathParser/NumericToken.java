package SimpleMathParser;

public class NumericToken extends SimpleToken
{
   private final double data;


   // Constructors


   public NumericToken(double data)
   {
       super(SimpleToken.NUMBER_TOKEN);
       this.data = data;
   }


   // Methods


   public double getData()
   {
       return this.data;
   }

   public String toString()
   {
       return "" + this.data;
   }
}
