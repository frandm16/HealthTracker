import { ScrollView } from 'react-native';

export default function PersonalScreen() {
  return (
    <ScrollView
      contentInsetAdjustmentBehavior="automatic"
      contentContainerStyle={{
        flexGrow: 1,
        padding: 20,
        backgroundColor: '#f4f7f2',
      }}
    >
    </ScrollView>
  );
}
