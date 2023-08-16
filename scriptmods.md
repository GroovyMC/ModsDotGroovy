(todo: finish script mod context docs)
### Script mods
On supported script mod loaders, you can either place your mods.groovy file in your script group's root directory (1) or
embed it directly inside the top of your main script file (2).

1. As a separate file:
```
└──📂 scripts
   └──📂 examplescript
      └──📄 mods.groovy
      └──📄 main.groovy
```

2. Embedded inside your main script file:
```
└──📂 scripts
   └──📄 examplescript.groovy
```

```groovy
package examplescript

ModsDotGroovy.make {
    // ...
}
```
