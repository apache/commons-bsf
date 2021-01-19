/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/*
   ooRexx (FOSS):   <http://www.ooRexx.org>
   BSF4Rexx (FOSS): <http://wi.wu-wien.ac.at/rgf/rexx/bsf4rexx/current/> or
                    eventually at <https://sourceforge.net/projects/bsf4rexx>
*/

/*********************************************************
 * A simple four function calculator, written in REXX    *
 *********************************************************/

/* *** create a res window                   */
res = .bsf~new("java.awt.TextField", 0)


/* *** create a panel of buttons                */
panel = .bsf~new("java.awt.Panel")
panel~setLayout(.bsf~new("java.awt.GridLayout", 4, 4))

buttons = "789*456/123-C0.+"
do i=1 to buttons~length
   label=buttons~substr(i, 1)
   button=.bsf~new("java.awt.Button", label)
   panel~add(button)

   if "*/-+"~pos(label)>0 then
      button~bsf.addEventListener("action", "", "call op '" ||label||"'")

   else if label="C" then
      button~bsf.addEventListener("action", "", "call clear")

   else
      button~bsf.addEventListener("action", "", "call press" label)
end


/* *** Place everything in the frame            */
frame = .bsf~bsf.lookupBean("frame")
frame~~setTitle("Object Rexx Calc") ~~resize(130, 200)
frame~~add("North", res) ~~add("Center", panel) ~~validate ~~show
frame~bsf.addEventListener("window", "windowClosing", "call bsf 'exit'")

/* *** Initialize the state of the calculator   */
mem = 0
nextOp = "+"
autoClear = 1                   /* true         */

do forever      /* get eventText and execute it as a Rexx program       */
   interpret .bsf~bsf.pollEventText
end
exit


press:                          /* handle data entry keys       */
   parse arg key

   if autoClear  then
      res~bsf.invokestrict("setText", "str", 0)

   if res~getText=="0" & key<>"." then
      res~bsf.invokeStrict("setText", "str", "")
      -- res~setText("str", "")

   if key="." then
      if pos(".", res~getText)>0 then
         key=""

   -- res~setText("str", res~getText || key)
   res~bsf.invokeStrict("setText", "str", res~getText || key)

   autoClear=0                  /* set it to false              */
   return

/*  *** handle arithmetic keys  */
op:
   parse arg key
   num=res~getText
   if      nextOp="+" then mem=mem+num
   else if nextOp="-" then mem=mem-num
   else if nextOp="*" then mem=mem*num
   else if nextOp="/" & num<>0 then mem=mem/num
   nextOp=key
   -- res~setText("str", mem)
   res~bsf.invokeStrict("setText", "str", mem)
   autoClear=1          /* set to true          */
   return

/* handle the "C" key   */
clear:
   mem=0
   nextOp=""
   -- res~setText("str", 0)
   res~bsf.invokeStrict("setText", "str", 0)
   return

::requires BSF.CLS    -- add Object Rexx proxy support

