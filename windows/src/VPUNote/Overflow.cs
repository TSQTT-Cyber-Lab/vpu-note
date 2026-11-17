using System.Globalization;
using System.Text.Json.Nodes;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace VPUNote.App;

/// Dev only (`overflow` on the debug pipe): walks every open window and lists what doesn't fit, so a
/// longer language (Vietnamese, Russian) is checked without eyeballing each screen. Cut: a one-line
/// TextBlock whose text is wider than the box it got (drawn with …, or simply clipped). Outside:
/// an element drawn past its window's edge. Wrapping text is fine unless its lines are clipped.
static class Overflow
{
    public static JsonArray Scan()
    {
        var found = new JsonArray();
        foreach (Window w in Application.Current.Windows)
        {
            if (!w.IsVisible || w.ActualWidth < 1) continue;
            Walk(w, w, found);
        }
        return found;
    }

    static void Walk(DependencyObject node, Window window, JsonArray found)
    {
        if (node is UIElement { IsVisible: false }) return;
        if (node is TextBlock t && t.ActualWidth > 0 && !string.IsNullOrEmpty(t.Text)) Check(t, window, found);
        else if (node is FrameworkElement fe && fe is not Window && fe.ActualWidth > 1 && Outside(fe, window) is { } by)
            found.Add(Entry("outside", window, fe, $"{by:0}px past the window", fe.GetType().Name));
        for (int i = 0; i < VisualTreeHelper.GetChildrenCount(node); i++) Walk(VisualTreeHelper.GetChild(node, i), window, found);
    }

    static void Check(TextBlock t, Window window, JsonArray found)
    {
        var dpi = VisualTreeHelper.GetDpi(t).PixelsPerDip;
        var face = new Typeface(t.FontFamily, t.FontStyle, t.FontWeight, t.FontStretch);
        var ft = new FormattedText(t.Text, CultureInfo.CurrentUICulture, t.FlowDirection, face, t.FontSize, Brushes.Black, dpi);
        if (t.TextWrapping == TextWrapping.NoWrap)
        {
            // The width the text wants vs the width it was given (its own box, or the parent's if that is tighter).
            if (ft.WidthIncludingTrailingWhitespace > t.ActualWidth + 1)
                found.Add(Entry("cut", window, t, $"needs {ft.WidthIncludingTrailingWhitespace:0}px, has {t.ActualWidth:0}px", t.Text));
        }
        else if (t.TextTrimming != TextTrimming.None || t.MaxHeight < double.PositiveInfinity)
        {
            ft.MaxTextWidth = Math.Max(1, t.ActualWidth);
            if (ft.Height > t.ActualHeight + 2) found.Add(Entry("cut", window, t, $"needs {ft.Height:0}px high, has {t.ActualHeight:0}px", t.Text));
        }
        if (Outside(t, window) is { } by) found.Add(Entry("outside", window, t, $"{by:0}px past the window", t.Text));
    }

    /// How far (px) the element's box reaches past its window's client area, or null.
    static double? Outside(FrameworkElement fe, Window window)
    {
        if (window.Content is not UIElement root || !fe.IsDescendantOf(root)) return null;
        var box = fe.TransformToAncestor(root).TransformBounds(new Rect(0, 0, fe.ActualWidth, fe.ActualHeight));
        var area = new Rect(0, 0, root.RenderSize.Width, root.RenderSize.Height);
        var by = Math.Max(Math.Max(area.Left - box.Left, box.Right - area.Right), Math.Max(area.Top - box.Top, box.Bottom - area.Bottom));
        // A scroll viewer's content is meant to run past its viewport.
        for (DependencyObject p = fe; p is not null; p = VisualTreeHelper.GetParent(p)) if (p is ScrollViewer) return null;
        return by > 2 ? by : null;
    }

    static JsonObject Entry(string kind, Window window, FrameworkElement fe, string detail, string text) => new()
    {
        ["kind"] = kind, ["window"] = window.GetType().Name, ["detail"] = detail, ["text"] = text.Length > 60 ? text[..60] : text,
    };
}
