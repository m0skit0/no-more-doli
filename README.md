# no-more-doli

Simple app to allow one click punching into our Dolicloud.

Just input your login/password once, then the app will take you everytime to the punching screen. Just click the PUNCH button to both sign in and sign out ;)

## Punching by intent from tasking apps

As of version 1.3, now app supports punching by Intents. This means tasking apps like Llama, Tasker, Automate, etc... can punch for you given a specific set of conditions.

The Intent is simple:

Action: org.m0skit0.android.nomoredoli.punch

Package: org.m0skit0.android.nomoredoli

Class: org.m0skit0.android.nomoredoli.PunchReceiver

For example in Llama, these are my conditions for launching the intent:

![](https://i.ibb.co/yBSYcm7/Screenshot-2019-05-24-12-00-16-262-com-kebab-Llama.png)
