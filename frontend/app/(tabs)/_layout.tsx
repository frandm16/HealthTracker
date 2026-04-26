import * as React from 'react';
import { Tabs, useRouter } from 'expo-router';
import { Pressable, Text, View } from 'react-native';

import { AuthContext } from '@/providers/auth-provider';

function getProfileInitials(name: string | undefined) {
  const tokens = name?.trim().split(/\s+/).filter(Boolean) ?? [];

  if (tokens.length === 0) {
    return 'P';
  }

  if (tokens.length === 1) {
    return tokens[0].slice(0, 2).toUpperCase();
  }

  return `${tokens[0][0] ?? ''}${tokens[1][0] ?? ''}`.toUpperCase();
}

function ProfileAvatarButton() {
  const auth = React.use(AuthContext);
  const router = useRouter();
  const initials = getProfileInitials(auth?.session?.user.name);

  return (
    <Pressable
      onPress={() => router.push('/auth')}
      style={{
        marginLeft: 16,
      }}
    >
      <View
        style={{
          width: 36,
          height: 36,
          alignItems: 'center',
          justifyContent: 'center',
          borderRadius: 999,
          backgroundColor: '#102114',
        }}
      >
        <Text
          style={{
            color: '#ffffff',
            fontSize: 13,
            fontWeight: '900',
          }}
        >
          {initials}
        </Text>
      </View>
    </Pressable>
  );
}

export default function TabsLayout() {
  return (
    <Tabs
      initialRouteName="nutrition"
      screenOptions={{
        headerLeft: () => <ProfileAvatarButton />,
        headerShadowVisible: false,
        headerStyle: {
          backgroundColor: '#f4f7f2',
        },
        headerTitleStyle: {
          color: '#102114',
          fontWeight: '900',
        },
        tabBarStyle: {
          height: 72,
          paddingTop: 8,
          paddingBottom: 10,
          backgroundColor: '#ffffff',
          borderTopColor: '#d9e4d3',
        },
        tabBarActiveTintColor: '#102114',
        tabBarInactiveTintColor: '#7b8b76',
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '800',
        },
      }}
    >
      <Tabs.Screen
        name="nutrition"
        options={{
          title: '',
          tabBarLabel: 'Nutrition',
        }}
      />
      <Tabs.Screen
        name="workout"
        options={{
          title: '',
          tabBarLabel: 'Workout',
        }}
      />
      <Tabs.Screen
        name="personal"
        options={{
          title: '',
          tabBarLabel: 'Personal',
        }}
      />
    </Tabs>
  );
}
