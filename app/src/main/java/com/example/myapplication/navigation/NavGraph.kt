package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.screens.chat.ChatScreen
import com.example.myapplication.screens.gost.GostPrijavaScreen
import com.example.myapplication.screens.gost.GostStatusScreen
import com.example.myapplication.screens.gost.MojiKvaroviScreen
import com.example.myapplication.screens.izboruloge.IzborUlogeScreen
import com.example.myapplication.screens.login.LoginScreen
import com.example.myapplication.screens.majstor.MajstorDetaljiScreen
import com.example.myapplication.screens.majstor.MajstorZadaciScreen
import com.example.myapplication.screens.menadzer.IzvestajScreen
import com.example.myapplication.screens.menadzer.MenadzerDetaljiScreen
import com.example.myapplication.screens.menadzer.MenadzerListaScreen
import com.example.myapplication.screens.menadzer.SobaQrScreen
import com.example.myapplication.screens.startup.StartupScreen

sealed class Screen(val route: String) {
    object Startup : Screen("startup")
    object IzborUloge : Screen("izbor_uloge")
    object GostPrijava : Screen("gost_prijava")
    object Login : Screen("login")
    object SobaQr : Screen("soba_qr")
    object MenadzerLista : Screen("menadzer_lista")
    object Izvestaj : Screen("izvestaj")
    object MojiKvarovi : Screen("moji_kvarovi")

    object GostStatus : Screen("gost_status/{kvarId}") {
        fun createRoute(kvarId: String) = "gost_status/$kvarId"
    }

    object MenadzerDetalji : Screen("menadzer_detalji/{kvarId}") {
        fun createRoute(kvarId: String) = "menadzer_detalji/$kvarId"
    }

    object MajstorZadaci : Screen("majstor_zadaci/{imeMajstora}") {
        fun createRoute(ime: String) = "majstor_zadaci/$ime"
    }

    object MajstorDetalji : Screen("majstor_detalji/{kvarId}/{imeMajstora}") {
        fun createRoute(kvarId: String, ime: String) = "majstor_detalji/$kvarId/$ime"
    }

    object Chat : Screen("chat/{kvarId}/{mojaUloga}/{zakljucan}") {
        fun createRoute(kvarId: String, mojaUloga: String, zakljucan: Boolean) =
            "chat/$kvarId/$mojaUloga/$zakljucan"
    }
}

@Composable
fun HotelFixNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Startup.route) {

        composable(Screen.Startup.route) {
            StartupScreen(
                onGost = {
                    navController.navigate(Screen.IzborUloge.route) {
                        popUpTo(Screen.Startup.route) { inclusive = true }
                    }
                },
                onMenadzer = { ime ->
                    navController.navigate(Screen.MenadzerLista.route) {
                        popUpTo(Screen.Startup.route) { inclusive = true }
                    }
                },
                onMajstor = { ime ->
                    navController.navigate(Screen.MajstorZadaci.createRoute(ime)) {
                        popUpTo(Screen.Startup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.IzborUloge.route) {
            IzborUlogeScreen(
                onGost = { navController.navigate(Screen.GostPrijava.route) },
                onZaposleni = { navController.navigate(Screen.Login.route) },
                onMojiKvarovi = { navController.navigate(Screen.MojiKvarovi.route) }
            )
        }

        composable(Screen.GostPrijava.route) {
            GostPrijavaScreen(
                onPrijavaPoslata = { kvarId ->
                    navController.navigate(Screen.GostStatus.createRoute(kvarId)) {
                        popUpTo(Screen.GostPrijava.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.GostStatus.route,
            arguments = listOf(navArgument("kvarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val kvarId = backStackEntry.arguments?.getString("kvarId") ?: return@composable
            GostStatusScreen(
                kvarId = kvarId,
                onOtvoriChat = {
                    navController.navigate(Screen.Chat.createRoute(kvarId, "GOST", false))
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onPrijavaUspesna = { korisnik ->
                    val ruta = if (korisnik.uloga == "MENADZER") {
                        Screen.MenadzerLista.route
                    } else {
                        Screen.MajstorZadaci.createRoute(korisnik.ime)
                    }
                    navController.navigate(ruta) {
                        popUpTo(Screen.IzborUloge.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MojiKvarovi.route) {
            MojiKvaroviScreen(
                onKvarKlik = { id -> navController.navigate(Screen.GostStatus.createRoute(id)) }
            )
        }

        composable(Screen.MenadzerLista.route) {
            MenadzerListaScreen(
                onKvarKlik = { id -> navController.navigate(Screen.MenadzerDetalji.createRoute(id)) },
                onGenerisiQr = { navController.navigate(Screen.SobaQr.route) },
                onIzvestaj = { navController.navigate(Screen.Izvestaj.route) },
                onOdjava = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.IzborUloge.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.MenadzerDetalji.route,
            arguments = listOf(navArgument("kvarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val kvarId = backStackEntry.arguments?.getString("kvarId") ?: return@composable
            MenadzerDetaljiScreen(
                kvarId = kvarId,
                onOtvoriChat = {
                    navController.navigate(Screen.Chat.createRoute(kvarId, "MENADZER", false))
                }
            )
        }

        composable(
            route = Screen.MajstorZadaci.route,
            arguments = listOf(navArgument("imeMajstora") { type = NavType.StringType })
        ) { backStackEntry ->
            val ime = backStackEntry.arguments?.getString("imeMajstora") ?: return@composable
            MajstorZadaciScreen(
                imeMajstora = ime,
                onKvarKlik = { id -> navController.navigate(Screen.MajstorDetalji.createRoute(id, ime)) },
                onOdjava = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.IzborUloge.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.MajstorDetalji.route,
            arguments = listOf(
                navArgument("kvarId") { type = NavType.StringType },
                navArgument("imeMajstora") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val kvarId = backStackEntry.arguments?.getString("kvarId") ?: return@composable
            val ime = backStackEntry.arguments?.getString("imeMajstora") ?: return@composable
            MajstorDetaljiScreen(kvarId = kvarId, imeMajstora = ime)
        }

        composable(Screen.SobaQr.route) {
            SobaQrScreen()
        }

        composable(Screen.Izvestaj.route) {
            IzvestajScreen()
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("kvarId") { type = NavType.StringType },
                navArgument("mojaUloga") { type = NavType.StringType },
                navArgument("zakljucan") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val kvarId = backStackEntry.arguments?.getString("kvarId") ?: return@composable
            val mojaUloga = backStackEntry.arguments?.getString("mojaUloga") ?: return@composable
            val zakljucan = backStackEntry.arguments?.getBoolean("zakljucan") ?: false
            ChatScreen(
                kvarId = kvarId,
                mojaUloga = mojaUloga,
                zakljucan = zakljucan,
                onNazad = { navController.popBackStack() }
            )
        }
    }
}