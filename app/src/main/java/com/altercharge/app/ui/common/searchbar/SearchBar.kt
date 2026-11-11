package com.altercharge.app.ui.common.searchbar

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altercharge.app.R
import com.altercharge.app.ui.designsystem.theme.AlterChargeTheme
import com.altercharge.app.ui.designsystem.theme.AlterRadius
import com.altercharge.app.ui.designsystem.theme.AlterTheme

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onMicClick: () -> Unit,

) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AlterRadius.Large))
            .background(AlterTheme.colors.background)
            .padding(vertical = 14.dp, horizontal = 20.dp)
    ){
        Image(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = null,
            colorFilter = ColorFilter.tint(AlterTheme.colors.contentNeutral)
        )

        TextField(
            value = "",
            placeholder = { Text(stringResource(R.string.search_bar_placeholder)) },
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle.Default.copy(fontSize = 20.sp)
        )

        Image(
            painter = painterResource(R.drawable.ic_mic),
            contentDescription = null,
            colorFilter = ColorFilter.tint(AlterTheme.colors.contentNeutral),
            modifier = Modifier.clickable {
                onMicClick()
            }
        )
    }
}

@Preview(name = "SearchBar(Light/KO)", locale = "ko")
@Composable
private fun SearchBarPreviewLightKo() {
    AlterChargeTheme() {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}

@Preview(name = "SearchBar(Light/KO)", locale = "ko")
@Composable
private fun SearchBarPreviewDarkKo() {
    AlterChargeTheme(darkTheme = true) {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}

@Preview(name = "SearchBar(Light/EN)", locale = "en")
@Composable
private fun SearchBarPreviewLightEn() {
    AlterChargeTheme() {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}

@Preview(name = "SearchBar(Dark/EN)", locale = "en")
@Composable
private fun SearchBarPreviewDarkEn() {
    AlterChargeTheme(darkTheme = true) {
        SearchBar(
            onValueChange = {},
            onMicClick = {}
        )
    }
}
