#************************************************************
#* A simple four function calculator, written in PerlScript *
#************************************************************/
# *** create a result window
$result = CreateBean("java.awt.TextField", "0");

# *** create a panel of buttons
$panel = CreateBean("java.awt.Panel");
$panel->setLayout( CreateBean("java.awt.GridLayout", 4, 4));

$buttons = '789*456/123-C0.+';
for(  $i= 0 ; $i< length($buttons); ++$i)
{
   $label = substr($buttons,$i,1);
   $button = CreateBean("java.awt.Button", $label);
   $panel->add($button);

   if(-1 !=  index("*/-+", $label))
   {
     $button->onaction("&op(\"$label\");");
   }
   elsif ($label eq "C") 
   {
      $button->onaction("&clear");
   }  
   else 
   {
      $button->onaction("&press(\"$label\");");
   }  
} 

# *** Place everything in the frame
$frame->title("PerlScript Calc");
$frame->resize( 130, 200);
$frame->add( "North", $result );
$frame->add( "Center", $panel );
$frame->validate();

# *** Initialize the state of the calculator
$mem = 0;
$nextOp = "+";
$autoClear = 0;

# *** handle data entry keys
sub  press 
{
   $key= $_[0];
   if( $autoClear ){ $result->text("0");}
   if($result->text eq "0" &&  $key ne "." ){ $result->text("");}
   if( $key eq "." && (-1 != index($result->text, "."))){  $key="";}
   $result->text( $result->text . $key);
   $autoClear=0;
}

#  *** handle arithmetic keys
sub op
{
   $key= $_[0];
   $num = $result->text+0;
   if( $nextOp eq "+" ){ $mem = $mem + $num;}
   if( $nextOp eq "-" ){  $mem = $mem - $num;}
   if( $nextOp eq "*" ){  $mem = $mem * $num;}
   if( $nextOp eq "/" ){ $mem = $mem / $num;}
   $nextOp = $key;

   $result->text ( "" . $mem);
   $autoClear=1;
}

sub clear
{
  $mem=0 ; $nextOp="+"; $result->text("0");
}
