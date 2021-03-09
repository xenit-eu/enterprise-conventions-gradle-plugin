package eu.xenit.gradle.enterprise.conventions.extensions.signing;

import java.io.File;
import java.net.URL;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugins.signing.SigningPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.util.GFileUtils;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

abstract public class AbstractSigningPluginSetup {

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    protected Project createProject(Action<Project> doBefore) {
        Project project = ProjectBuilder.builder().build();
        doBefore.execute(project);

        project.getPluginManager().apply(AutomaticSigningPlugin.class);
        project.getPluginManager().apply(MavenPublishPlugin.class);
        project.getPluginManager().apply(SigningPlugin.class);

        PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);

        publishing.getPublications().create("mavenJava", MavenPublication.class, publication -> {
        });

        return project;
    }

    protected void configureSigningWithPgp(Project project) {
        URL secRingResource = AbstractSigningPluginSetup.class.getResource("secring.gpg");
        File gpgSecring = project.getProjectDir().toPath().resolve("secring.gpg").toFile();
        GFileUtils.copyURLToFile(secRingResource, gpgSecring);
        project.getExtensions().getExtraProperties().set("signing.keyId", "32C2FC7D");
        project.getExtensions().getExtraProperties().set("signing.password", "This is a fake key");
        project.getExtensions().getExtraProperties().set("signing.secretKeyRingFile", "secring.gpg");
    }

    protected void configureSigningWithInMemory(Project project) {
        environmentVariables.set("SIGNING_PRIVATE_KEY", "-----BEGIN PGP PRIVATE KEY BLOCK-----\n"
                + "\n"
                + "lQWGBGBGXrgBDADTXJ12RZ29Q1D/4e2Ik+hxNcuP7HDtb7PuVfiZobjGdsKFXdFl\n"
                + "vq0GkoIgRO9veRpHFeVobBb2t2M+61jFazW7ObIFIuaTs635a1HJY1NYRge60O/l\n"
                + "0HXu2Eh99CGQebZx9mqvit7r0ChL1XNDRVNcc+lmTBx/4nJPahXSkTo+Pip8OTCm\n"
                + "d3dPsjRI63TEjnwONjxtYv5mSGQJNpPj002piKVNIyHeDh80OQbVVPsC+PdsFYwT\n"
                + "QoZVzo+Qvg/mT7ArhB+QSKSUQi+0d9VM9w6FSelvcSQmklq2eBcCY6Pf7S+2DY8n\n"
                + "QdftsrsakRxOyoYMlTekA0SN/MmwN+gZH2nwvicD660oG8Ryapb3cL2jbElI6q98\n"
                + "JABj/vNFTX5E6JDWCr8mMBmJ0VPdETDkI4bJpeB/U5Oje37z4JhEYOLDCrt0ay60\n"
                + "dv9yNRFNsUH3Gh3uHPJtc7lmeociUyjmyjtQtfiqYRziCiVyodqtLSxrLHeOzvQN\n"
                + "vjPsPdCAVLcAOKcAEQEAAf4HAwIueZMdOmkoDf+jHS5TSehMPefF5H1KkwnOlG32\n"
                + "m3YnQJw7R0sWXo1t5QxxPEr0hdIHTFKhCBCh8R4xCqQFewTr9LDAWj8VXzPhhiRL\n"
                + "NBJurhHeAbAMnjk7BXvjYDVdEHXgSt04bjR9ubHixyxB2APNZldEYcy+4FxbufXc\n"
                + "b/INiRdXTtqLPfJ4qhc3VpvbKCgvZDjN8zAJmaU2QySrRtoJYVXlWdUH06/ukXWh\n"
                + "zVWr7G7slekV8+bb04aezYgVFDnskur3HY4XYF/5efUOuU3XxzmhB9r2qSOkd8CM\n"
                + "8exr7WpUEUl+DJu1fenC5GYglI/XfdAf6pTJKzDSMOHFhPJpVr+vsPAukTan6i+J\n"
                + "b0dQEHzvHWWFda8Yb4hdyUk6CXmX6Qj6qBv5SvvQiWGtOT2O+17PtDFx43A8aniL\n"
                + "8Q5SOffYh0J3boqaZPn9slV+IYtRZJ/AULTBqeEg/4gYoPdJgXxzwD3xHKhsbMgJ\n"
                + "mBtWzgtWcR3sEZRcIZuTL4lluXbzRV3Rdo+UzXbrvX0nqB0yBYbfpwkLo8m9fsEe\n"
                + "oCDW6TWtXW5q/lnjdKbsg0VE4MSQFF1/bG8ZRf8qJBuLLP5gcTgeGNu/atHM0zAW\n"
                + "vNhqHWg8le6oIcBhRzojJ/wcQbhJz/ITyOBJSlCb20QXC4xEYLYPTmDZpPSi5slu\n"
                + "3XyjS/kgYy2goTEKFDxuHaZwnauyn44XveZ5/quJhFGAG30q8A4MtF0wyXkT1a1z\n"
                + "hjkeVEnZZXnWkTnrNRCu+af+H4YoJBBU/q4qzGAMvf+scGKF+2cTey+iigrYQqeI\n"
                + "IJSkyxFxgz0wikcSgZKGZFT0qdK9ccWhEoWCcrVq26WARjjfcRZg4zSKkoQ6PLjT\n"
                + "qm1Bp+tJkqohuomfj1CM4PF8JoaWzG/18rQ+kjLvVorTo39o9wibTil424Da0J2F\n"
                + "0n2isx5Y12kajUmJ3HetjSLQTdfg0RmmcHRhLGEwV79jsZyHqyux1dTEVN2n4fHR\n"
                + "GKT02gZz+1RcQiGDuae0mvsex8xU0eolSNuLmlh3DKv/OHr+zg1tAZqwlvmirk4p\n"
                + "0t3ycjJ8Vvgs41KHlBmqFcD+VHWMAzkAHGe7oZF9ZQcN1CytU5wMIKaKEWs7mCBT\n"
                + "7m8t/5RpFLX79+fRHLEHuws+BtPTlCvlRduNMPCDai0H9T6Xi7kdwFBjqJK1pHqy\n"
                + "BJgI0H6Co05jnt9FNlByArqRbVxthxpXQvo/IVqhVVcUcwjk5s382NEAHKGq/9Gn\n"
                + "2YPbI30tbqKr10glihb2eH0+eDnNNCPgBHKuu5IP6jKQEIl/2ZWiA9fHx/+Pr0cg\n"
                + "QkkkLlNFm+OL9gPJlxb/yVKgHAlQZfHPCbQfRmFrZSBLZXkgPGZha2VAZXhhbXBs\n"
                + "ZS5pbnZhbGlkPokBzgQTAQoAOAIbAwULCQgHAgYVCgkICwIEFgIDAQIeAQIXgBYh\n"
                + "BGUuobj/KOlMbLr1qfLqSpAywvx9BQJgRl/IAAoJEPLqSpAywvx9Id4MAJrupSaj\n"
                + "UsFw0Dyiksz7J0d17MMGGhDpozl6AtePS1FHVgQ7vFUC2jh9chgsSvx6rV2o9IRP\n"
                + "6l04PE/tdhTH+UsR2KUdlHP9x6iYPTpve5NFLXAoOnzmd4TGSgNwKHQmTrRL/9M+\n"
                + "5h+DF1jKku9Q6tNN/hdQJ1WSDIP19mEt7I5Ccm21UqQB6FqLsNAqJPgJLl+zuwZD\n"
                + "SDDmchj6Iz3Yg/40EXHQ/PxM6vM/2LPVgzsUHO+eiLEZqlY9uAYW8q/aprmRFO1v\n"
                + "Wlbr1OimjuSFCpriPClpfd/+jPMvTXJp6sOTpQJPwcVvDbgpcWqkM4EGP/LP4Qtt\n"
                + "NKQ553W+13fTT6jsC6sg6GVwuIpfpcN4PJZcwzCiiJ3TECtHmSVBZwlgPjAHklHp\n"
                + "/lzyrmDvevDCecmFgYMZ9rA33ya4b3ydEUiZPvSaAEeauCDZll1SzwjiQIK6dD4g\n"
                + "Q93amVI3AstcOzHt8LKIDa+nEg21YupCuu7QQCDFX4huqLiBKlAOML4XzZ0FhgRg\n"
                + "RoetAQwAy64hY4XXuzDHV/PsNsbtWx88SPM0eRK5aMSRrxnoPfK3wdt9fH6yc8HG\n"
                + "we0SYszUmAg14zs6jVrSaKadhyquyXGWkTKs+qUppg3pTqwaqEIIFam0FeDwAhjM\n"
                + "w/l9Tr13gumcYpuDYVByzj4jOlnoHoayQyQfVuzqO9tBlJY2xWtU18ciG0Dd7TWa\n"
                + "i0C/ClvkKJc1WgnTewBFO1TpOLK2aR50DIQUIaweEUlnL2phlDmW2NZFqCk4zmMK\n"
                + "6aY1TdyY+pUcDnYSEW3Ksbl/hI9HQ2gNL3EuySrJSmCD7Wk6ozjT4Hj1HNFlw+S0\n"
                + "tSpk2V3JksZ2fH0j+R77GCVhbU+XyQbxfwwJxbMzjiB+yRaSVBFVy+Srp/AB591E\n"
                + "KsTIvnrGFNdq71XRthVTuAwgOjfy4Ks9Ub7wyZeRkXXJU0kv4PSJWU+WbMCrtH6o\n"
                + "r76QDGPyqwR7lSnnEVokxZeJgIt6SvAsUBVC21Bx9nytZ0cHdWHfTvmh5lfFYECo\n"
                + "WMFEyCxDABEBAAH+BwMCKDx8EZ4C6db//uhqOrkiH5u+xdfgidcU3mfDa//Dvli3\n"
                + "dnE/uWx+fYzSGNOjSk2fjFkOcngQLHjlkC4v4b85KGLrSseLgPP3OTgQcFGOjwbU\n"
                + "lLZQqB2BVDCZ23O8K5emYm+jKjSHX/jKJbtsxPjEW04s/nKZvcuwxaSZge5V2T2B\n"
                + "M7kBGwIfRUg9GFyNn9KhmxyiIcigqYJ3qb7vddI8Gcf5431aCCQgyjGbgNgyGc5D\n"
                + "iKnbeTTJ8ceqWgG6FU0Ca4oqd+AxG/lrYOxuWm0uwDUK2nui6tlO8kdJst5J7mG/\n"
                + "3b/Q9LbLjM6jGJzTf1h9Iv6mtum8JkASq2sbruy44xoAKithdASe1z17meUkJmxE\n"
                + "QgzedRdff6hu5aFQ7qWW8F56GYtfdTrC+kjquVNL5CvVkhImhRfSad8o0NpQNfAT\n"
                + "Qrf0ZAD4hjGdxkq8XDbNmmGyccL7jaV+jv6O+vQsjIn760DUtKQrDKRciHh+Ygn4\n"
                + "sFIsvZ1hEJw25YIojiwG0lIG84xFSYG624eu+xU1ZPZx48K7v04FmNhuXKu3SzKC\n"
                + "WlclVzoY0yjhaXi7iJTBW4aJYUQ4DeALtz5ryEA3dZXyesbCALXb1sJFKU9kRMti\n"
                + "VKwLaa1Dj6siFMCXRIQmPlXehsnZv3BBBXwL537aMPeYSHw5BAVhRcRGwitJhMLk\n"
                + "s805LYzrc5sGcIyRKnbbN97lfVNLO4TbwE3hxHMTCPvZDAQEP455Wf106wsX151G\n"
                + "CZgrEE0EhTcwLVHpxJb5CVTFUetjN1XielrPSYO6k/avXnbwXp254GT4ZIyOPXuD\n"
                + "/9tRKTiU1TmYfio9rOgY1LUNjGQGMngcM/nI0kzKgnM87AP+/7AcRqf2VhSy1paW\n"
                + "EOBVJ3CYTaGRK7kaicGIlbZUsOJ5OG0PuLF0tAZCSZ0H6BgrgTlqzWqZFyPUVmfW\n"
                + "miwX5X2Or0FqFweibMszamuU0X9dbzg3e4WvzO6miLIm6esXWkxcWQTxFVwrs1fk\n"
                + "1ecSIwrr3qgqxy2ZQ6jHANXstofykhyyY46UeKhXs1NPsKRCFqqRp7kfxOM7J130\n"
                + "ey9z/eX4ZfexnPJO+RmimBhSYavkJPE11pLUQoB9tGF+AGna9AHKejjP+BAKjgaI\n"
                + "J8RY17yZ6vjUI65Aqibiq97uqrYz3aCkp3LzBuwT2nKYjyyRGckQqe5STGfhSPbR\n"
                + "LaHFTHVifkxQPSw62ZGLWpQO+nPmVAJ33cNw5bJ3DXqWTBaCYxbveR8WTDegrHLZ\n"
                + "ptydDXCQo4Dy2as3x9Z4fEY/ZkzI22hDyQlJ/djVgnhAxUUGa/cQX7+oKvisuYSG\n"
                + "593gYA4HXVXMy/hoh5mbXpUb1T+JA2wEGAEKACAWIQRlLqG4/yjpTGy69any6kqQ\n"
                + "MsL8fQUCYEaHrQIbAgHACRDy6kqQMsL8fcD0IAQZAQoAHRYhBI4f6G0qzfBBz9K/\n"
                + "T9TBwRjpnP8NBQJgRoetAAoJENTBwRjpnP8NE7kL+gOGr5DxhaqEQHegbFTK7q43\n"
                + "D34NLZM5GakXCbkaLOspsWiqc5kSr6K1edWSJQcHU5ed0MMJXRfdAINVOFmvUkdk\n"
                + "X+Yr2WwXNdBH0ne+0fuqPcXMrBr+9x6+jvDs+NdO6u0uzifA4HdwxcEkF+5N1/SM\n"
                + "Dx+cqy5jmoH/XWqpukjKR5ipKURTyJ9Ivaau4YJo9plL+KpHTjgplR0VIjPk6CNL\n"
                + "8MR3tIlnXZcrzEyb3Hfx/KJeOtqvoD3H+s4bLyiL6sQYbWKoddxuYVYB9lRLR5Fl\n"
                + "rqqY1CoSxe3LzFZ5OeXzDZW2yrRE3dkoEsuXyh/9OsvEzYDiqRViPv90AlJprMO2\n"
                + "MTuh2U8/lILpc4/tWVIcVMtDiwDAvni5WJzFP5vViCGuDLWkYH4TLyLGok+8UJDm\n"
                + "5M2yDwG8gfyty3qL6yAhseUud5t/kpzZfbMcE0bznnLBeJcLInZa18xJHXd3dF4y\n"
                + "4tPqNgZhgkSQizWIKtNaqGgS9K7crL8aa79jP1JKIeDPDAC91Ey/fo9aWfzGsCIM\n"
                + "UvDXbK2GNjOsgONrb8zJrk3yUjQbcS8OaNgiaunWC9aEty/Xr3qCfJY9h2bmz/Sk\n"
                + "8lHVDGS53prmR0Wohi0NPwPAwAgYho5fGArssiOSu/LQ82JKLChLanfcSROfh+LF\n"
                + "pqVOuGlTMuccVXy+fwndr0Y2Km8vkVgQK9dnHc6TRG1mDDluyQ7Fu42HhzP9N6G6\n"
                + "x11B9sydhv3S/KY1eB52AwTUyUTMnNMW1frVbpGNxpGVW94jsrzsBjzTk5xKsKpR\n"
                + "yD9jf8DmnoYSCpiRseR2IK9JRNqKPBZEj7AH0ul5m4LF5E1gOvo/S0bprTbOFzu1\n"
                + "rmJ/WVF2IeWntJQq1j/A5KQhsttsaAp0jiOOsGl6oEoZm33+Fo2IkEmKOjPQuaCh\n"
                + "ZUvhVj8BuFQ6bYEr4xso9NNLE1cCF78a/Bc+WuXzNoKDlSWSVf2570qaSd1+iQ3r\n"
                + "9JcB96qyv2lSi4Xr+vHOLVVCq7GqY1QQihFClwnTkOCs3NY=\n"
                + "=+zJN\n"
                + "-----END PGP PRIVATE KEY BLOCK-----\n");
        environmentVariables.set("SIGNING_PASSWORD", "This is a fake key");
    }

    protected void configureSigningWithAgent(Project project) {
        project.getExtensions().getExtraProperties().set("signing.gnupg.keyName", "32C2FC7D");
        project.getExtensions().getExtraProperties().set("signing.gnupg.passphrase", "This is a fake key");
    }
}
