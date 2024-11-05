using System.ComponentModel;

namespace doorOpener.Models;

public static class EnumExtensions
{
    /// <summary>
    /// Rückgabe eines Attribute-Objekts eines Enum-Elements
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="value"></param>
    /// <returns></returns>
    public static T? GetAttribute<T>(this Enum value) where T : Attribute
    {
        var type = value.GetType();
        var memberInfo = type.GetMember(value.ToString());
        var attributes = memberInfo[0].GetCustomAttributes(typeof(T), false);
        return (attributes != null && attributes.Length > 0)
            ? (T)attributes[0]
            : null;
    }

    /// <summary>
    /// Rückgabe des Description-Attribute-Eintrags eines Enum-Elements
    /// </summary>
    /// <param name="value"></param>
    /// <returns></returns>
    public static string GetDescription(this Enum value)
    {
        var attribute = value.GetAttribute<DescriptionAttribute>();
        return attribute == null ? value.ToString() : attribute.Description;
    }
}