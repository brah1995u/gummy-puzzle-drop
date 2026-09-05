package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.components.CandyPanel
import studio.cortex.gummypuzzledrop.ui.components.GameLogo
import studio.cortex.gummypuzzledrop.ui.components.GummyNavigationRow

@Composable
fun MetaScreenScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onHome: () -> Unit = onBack,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bg_sprinkles),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(CandyDeepPurple.copy(alpha = .24f)))
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GummyNavigationRow(onBack = onBack, onHome = onHome)
            Spacer(Modifier.height(4.dp))
            GameLogo(compact = true)
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                modifier = Modifier.fillMaxWidth(),
                color = CandyCream,
                fontSize = 26.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                subtitle,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp),
                color = CandyMint,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            CandyPanel(Modifier.fillMaxWidth().weight(1f), contentPadding = 10.dp, content = content)
        }
    }
}
