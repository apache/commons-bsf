/* pick up the center panel bean, Object Rexx program modelled after ui.nrx */
/*
   ooRexx (FOSS):   <http://www.ooRexx.org>
   BSF4Rexx (FOSS): <http://wi.wu-wien.ac.at/rgf/rexx/bsf4rexx/current/> or
                    eventually at <https://sourceforge.net/projects/bsf4rexx>

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2003-2006 Rony G. Flatscher

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.
    -----------------------------------------------------------------------------
*/

p = .bsf~bsf.lookupBean("centerPanel")  -- reference the entry in BSF registry, put there by
                                    -- the Java program "ScriptedUI.class"

/* set the layout manager to border */
p~setLayout(.bsf~new("java.awt.BorderLayout"))

/* add a few things */
p~add("Center", .bsf~new("java.awt.Label",     "Middle from Object Rexx"))
p~add("North",  .bsf~new("java.awt.TextField", "North text from Object Rexx"))
p~add("South",  .bsf~new("java.awt.TextField", "South text from Object Rexx"))
p~add("East",   .bsf~new("java.awt.Button",    "Inner east text from Object Rexx"))
p~add("West",   .bsf~new("java.awt.Button",    "Inner west text from Object Rexx"))

/* configure p a bit */
p~setBackground(.bsf~bsf.getStaticValue("java.awt.Color", "green"))

/* configure the frame that p is in */
f=p~getParent
f~setTitle("Hello from Object REXX (title reset from Object Rexx)")

::requires BSF.CLS    -- get Object Rexx wrapper support for BSF

