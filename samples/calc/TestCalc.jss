/*********************************************************
'* A simple four function calculator, written in JScript *
'*********************************************************/

// *** create a result window
var result = CreateBean("java.awt.TextField", "0")

// *** create a panel of buttons
var panel = CreateBean("java.awt.Panel");
panel.setLayout( CreateBean("java.awt.GridLayout", 4, 4));

buttons = new String("789*456/123-C0.+");
for(  i= 0 ; i< buttons.length; ++i)
{
   label = buttons.substring(i,i+1);
   var button = CreateBean("java.awt.Button", label);
   panel.add( button);

   if(-1 != "*/-+".indexOf(label))
     button.onaction="op(\"" + label + "\")";
   else if (label=="C") 
     button.onaction="mem=0 ; nextOp=\"+\" ; result.text=\"0\";"
   else 
     button.onaction="press(\"" + label + "\");"
} 

// *** Place everything in the frame
frame.title = "JScript Calc";
frame.resize( 130, 200);
frame.add( "North", result );
frame.add( "Center", panel );
frame.validate()

// *** Initialize the state of the calculator
mem = 0
nextOp = "+"
autoClear = true

// *** handle data entry keys
function press (key)
{
   if( autoClear ) result.text="0";
   if(result.text=="0" &&  key!= "." ) result.text = "";
   if( key=="." && (-1 != result.text.indexOf(".")))  key="";
   result.text = result.text + key;
   autoClear=false;
}

//  *** handle arithmetic keys
function op(key)
{
   err=0;
   num = parseFloat(result.text); //+0;
   if( nextOp == "+" ) mem = mem + num;
   if( nextOp == "-" )  mem = mem - num;
   if( nextOp == "*" )  mem = mem * num;
   if( nextOp == "/" )  mem = mem / num;
   nextOp = key

   result.text = "" + mem;
//   If err>0 then result.text="ERROR" : mem=0 : nextOp="+"
   autoClear=true;
}
